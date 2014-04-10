import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Linking client
 */
public class LnkingClient {

    private String ip = "127.0.0.1";
    private int port = 60000;


    public byte[] call(byte[] sendbuf) {
        Socket socket = null;
        OutputStream os = null;
        byte[] recvbuf = null;
        try {
            socket = new Socket(ip, port);
            socket.setSoTimeout(10000);

            os = socket.getOutputStream();
            os.write(sendbuf);
            os.flush();

            InputStream is = socket.getInputStream();
            recvbuf = new byte[6];
            int readNum = is.read(recvbuf);
            if (readNum < 6) {
                throw new RuntimeException("报文头长度读取错误");
            }
            int msgLen = Integer.parseInt(new String(recvbuf).trim());
            recvbuf = new byte[msgLen];

            readNum = is.read(recvbuf);
            if (readNum != msgLen - 6) {
                throw new RuntimeException("报文读取异常");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert os != null;
                os.close();
                socket.close();
            } catch (IOException e) {
                //
            }
        }
        return recvbuf;
    }

    private String getRequestMsg8848(String body) {
        String header = "1.0" +
                "123456789012345678" +
                "0000" +
                "1538848" +
                "999999999" +
                "123456789012" +
                "FIS153" +  //userid 6
                "SBS   " +  //appid 6
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
                "MAC12345678901234567890123456789";

        return header + body;
    }


    public static void main(String... argv) {

        try {

            Schema schema = AvroSchemaManager.getSchema("schemas/M8848.avsc");
            GenericDatumWriter<GenericData.Record> datumWriter = new GenericDatumWriter<>(schema);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, baos);
            GenericData.Record record = new GenericData.Record(schema);
            record.put("orgidt", "orgidt");
            record.put("batseq", "batseq");
            record.put("pastyp", "pastyp");
            record.put("inpflg", "inpflg");
            record.put("sbknum", "sbknum");
            record.put("wrkunt", "wrkunt");
            datumWriter.write(record, encoder);
            encoder.flush();

            System.out.println(new String(baos.toByteArray()));

           /* LnkingClient client = new LnkingClient();
            String message = client.getRequestMsg8848(baos.toString("GBK"));
            System.out.printf("发送报文:%s\n", message);

            int len = message.getBytes("GBK").length;
            String strLen = "" + (len + 6);
            for (int i = strLen.length(); i < 6; i++) {
                strLen += " ";
            }
            byte[] recvbuf = client.call((strLen + message).getBytes("GBK"));
            System.out.printf("接收报文：%s\n", new String(recvbuf, "GBK"));*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
