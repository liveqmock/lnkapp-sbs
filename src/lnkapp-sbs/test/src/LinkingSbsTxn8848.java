import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import org.apache.avro.util.Utf8;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Linking client
 */
public class LinkingSbsTxn8848 {

    private String ip = "127.0.0.1";
    private int port = 60006;


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
            int msgLen = Integer.parseInt(new String(recvbuf, "UTF-8").trim()) - 6;
            recvbuf = new byte[msgLen];

            readNum = is.read(recvbuf);
            if(readNum != msgLen) {
                throw new RuntimeException("报文长度和接收长度不一致");
            }
            System.out.println("实际接收长度：" + readNum + " 报文头长度字段值：" + msgLen);
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

    private String getRequestMsg(String txnCode, String body) {
        String header = "1.0" +    // 3 version
                "123456789012345678" + // 18 serialNo
                "0000" + // 4-rtnCode
                txnCode + "  " + // 7-txnCode
                "999999999" + // 9-branchId
                "123456789012" + // 12-tellerId
                "FIS153" +  //userid 6
                "SBS   " +  //appid 6
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
                "MAC12345678901234567890123456789";

        return header + body;
    }


    public static void main(String... argv) {

        try {

            Schema schema = AvroSchemaManager.getSchema("schemas/code/Tia88480.avsc");
            GenericDatumWriter<GenericData.Record> datumWriter = new GenericDatumWriter<>(schema);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, baos);
            GenericData.Record record = new GenericData.Record(schema);
            record.put("pastyp", "1");
            record.put("inpflg", "23");
            record.put("sbknum", "56");


            datumWriter.write(record, encoder);
            encoder.flush();
            baos.close();
            String msg = new String(baos.toByteArray(), "UTF-8");
            System.out.println(msg);

            LinkingSbsTxn8848 client = new LinkingSbsTxn8848();
            String message = client.getRequestMsg("88480", msg);
            System.out.printf("发送报文:%s\n", message);

            int len = message.getBytes("UTF-8").length;
            String strLen = "" + (len + 6);
            for (int i = strLen.length(); i < 6; i++) {
                strLen += " ";
            }
            byte[] recvbuf = client.call((strLen + message).getBytes("UTF-8"));
            System.out.printf("接收报文:%s\n", new String(recvbuf, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
