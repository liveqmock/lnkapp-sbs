import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.util.ByteBufferOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by lenovo on 2014-04-24.
 */
public class AvroDataFileTest {
    public static void main(String[] args) {

//        writeToFile();
//        read();
        writeToBytes();
    }

    public static void writeToFile() {
        Schema schema = null;
        try {
            schema = new Schema.Parser().parse(AvroDataFileTest.class.getResourceAsStream("test.avsc"));
            GenericRecord datum = new GenericData.Record(schema);
            datum.put("param1", "中文中文");
            datum.put("param2", "english");
            File file = new File("data.avro");
            file.deleteOnExit();
            DatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>(schema);
            DataFileWriter<GenericRecord> dataFileWriter =
                    new DataFileWriter<GenericRecord>(writer);
            dataFileWriter.create(schema, file);
            dataFileWriter.append(datum);
            dataFileWriter.close();
            System.out.println("call writeToFile() over.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] writeToBytes() {
        Schema schema = null;
        try {
            schema = new Schema.Parser().parse(AvroDataFileTest.class.getResourceAsStream("test.avsc"));
            GenericData.Record record = new GenericData.Record(schema);
            record.put("param1", "中文中文");
            record.put("param2", "english");
            GenericDatumWriter<GenericData.Record> writer = new GenericDatumWriter<>(schema);
//            ByteBufferOutputStream bbos = new ByteBufferOutputStream();
            ByteArrayOutputStream bbos = new ByteArrayOutputStream();
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, bbos);
            writer.write(record, encoder);
            encoder.flush();
//            ByteBuffer buffer = null;
//            System.out.println(bbos.);
//            Charset cs = Charset.forName("UTF-8");
            System.out.println(new String(bbos.toByteArray(), "UTF-8"));
//            for (ByteBuffer bb : bbos.getBufferList()) {
//                System.out.println(cs.decode(bb));
//            }
            bbos.close();
            // TODO
            System.out.println("call writeToBytes() over.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void read() {
        File file = new File("data.avro");
        if (!file.exists()) {
            System.out.println("file not exist.");
            return;
        }
        DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>();
        try {
            DataFileReader<GenericRecord> dataFileReader =
                    new DataFileReader<GenericRecord>(file, reader);
            System.out.println("Schema:" + dataFileReader.getSchema().getName());
            GenericRecord record = dataFileReader.next();
            System.out.println("record's schema:" + record.getSchema().getName());
            System.out.println("record's param1:" + record.get("param1"));
            System.out.println("record's param2:" + record.get("param2"));
            System.out.println(dataFileReader.hasNext());
            for (String key : dataFileReader.getMetaKeys()) {
                System.out.println(key + " : " + new String(dataFileReader.getMeta(key)));
            }
            System.out.println("call read() over.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
