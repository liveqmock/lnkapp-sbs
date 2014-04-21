import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;

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
        String header = "1.0" +    // 3 version
                "123456789012345678" + // 18 serialNo
                "0000" + // 4-rtnCode
                "1538848" + // 7-txnCode
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

            Schema schema = AvroSchemaManager.getSchema("schemas/M8848.avsc");
            GenericDatumWriter<GenericData.Record> datumWriter = new GenericDatumWriter<>(schema);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, baos);
            GenericData.Record record = new GenericData.Record(schema);
            record.put("batseq", "11111");
            record.put("orgidt", "010");
            record.put("pastyp", "1");
            record.put("inpflg", "23");
            record.put("sbknum", "50");
            record.put("wrkunt", "Big");
            record.put("funcde", "4");
            // // 4-增 3-删 2-改 0-单笔 1-多笔

            record.put("depnum", "jigou");
            record.put("stmadd", "Middle");
            record.put("intnet", "中文");
            record.put("engnam", "hangyeA");
            record.put("regadd", "regadd");
            record.put("coradd", "coradd");
            record.put("cusnam", "zhangsan");
            record.put("begnum", "0");

            datumWriter.write(record, encoder);
            encoder.flush();
            baos.close();
            String msg = new String(baos.toByteArray());
/*
            GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, msg);
            GenericData.Record record2 = new GenericData.Record(schema);
            datumReader.read(record2, decoder);

            System.out.println(record2.get("cusnam").toString());
            System.out.println(record2.get("funcde"));
            System.out.println(record2.get("wrkunt"));
            System.out.println(record2.get("wrkunt").getClass().getName());*/

            LinkingSbsTxn8848 client = new LinkingSbsTxn8848();
            String message = client.getRequestMsg8848(msg);
            System.out.printf("发送报文:%s\n", message);

            int len = message.getBytes("GBK").length;
            String strLen = "" + (len + 6);
            for (int i = strLen.length(); i < 6; i++) {
                strLen += " ";
            }
            byte[] recvbuf = client.call((strLen + message).getBytes("GBK"));
            System.out.printf("接收报文:%s\n", new String(recvbuf, "GBK"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
