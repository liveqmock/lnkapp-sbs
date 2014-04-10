package org.fbi.sbs.helper;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.content.text.Generic;

import java.io.IOException;
import java.io.InputStream;

/**
 * 模式文件管理
 */
public class AvroSchemaManager {
    public static final Logger logger = LoggerFactory.getLogger(AvroSchemaManager.class);

    public static Schema getSchema(String schemaFileName) {
        InputStream inputStream = AvroSchemaManager.class.getClassLoader().getResourceAsStream(schemaFileName);
        try {
            Schema schema = new Schema.Parser().parse(inputStream);
            return schema;
        } catch (IOException e) {
            logger.error("Schema文件读取错误");
            throw new RuntimeException(e);
        }
    }

    public static GenericData.Record getSchemaObj(String txncode, String serialStr) throws IOException {
        Schema schema = AvroSchemaManager.getSchema("schemas/M" + txncode + ".avsc");
        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, serialStr);
        GenericData.Record record = new GenericData.Record(schema);
        record.
        datumReader.read(record, decoder);
        return record;
    }
}
