package org.fbi.sbs.helper;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.fbi.sbs.domain.Tia;
import org.fbi.sbs.domain.Toa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 模式文件管理
 */
public class AvroSchemaManager {
    public static final Logger logger = LoggerFactory.getLogger(AvroSchemaManager.class);
    private static final String SCHEMA_PATH = ProjectConfigManager.getInstance().getProperty("sbs.schema.path");
    private static final String SCHEMA_PREFIX = ProjectConfigManager.getInstance().getProperty("sbs.schema.prefix");
    private static final String SCHEMA_SUFFIX = ProjectConfigManager.getInstance().getProperty("sbs.schema.suffix");

    public static Schema getSchema(String schemaFileName) {
        InputStream inputStream = AvroSchemaManager.class.getClassLoader().getResourceAsStream(schemaFileName);
        try {
            Schema schema = new Schema.Parser().parse(inputStream);
            return schema;
        } catch (IOException e) {
            logger.info("Schema文件读取错误", e);
            throw new RuntimeException(e);
        }
    }

    public static Tia decode(String txnCode, byte[] bytes) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String strRequestBody = new String(bytes);
        logger.info(" Request Schema:[" + txnCode + "]" + strRequestBody);

        Schema schema = AvroSchemaManager.getSchema(SCHEMA_PATH + SCHEMA_PREFIX + txnCode + SCHEMA_SUFFIX);
        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, new String(bytes));
        GenericData.Record record = new GenericData.Record(schema);
        datumReader.read(record, decoder);

        String className = "org.fbi.sbs.domain.Tia" + txnCode;
        Class clazz = Class.forName(className);
        Tia tia = (Tia)clazz.newInstance();
        tia.from(record);
        return tia;
    }

    public static byte[] encode(String formCode, Toa toa) throws IOException, IllegalAccessException {
        Schema schema = AvroSchemaManager.getSchema(SCHEMA_PATH + formCode + SCHEMA_SUFFIX);
        GenericDatumWriter<GenericData.Record> datumWriter = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, baos);
        GenericData.Record record = new GenericData.Record(schema);
        toa.to(record);
        datumWriter.write(record, encoder);
        encoder.flush();
        baos.close();
        return baos.toByteArray();
    }

}
