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
import org.apache.commons.lang.StringUtils;
import org.fbi.sbs.domain.Tia;
import org.fbi.sbs.domain.Toa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ģʽ�ļ�����
 */
public class AvroSchemaManager {
    public static final Logger logger = LoggerFactory.getLogger(AvroSchemaManager.class);
    public static final String SCHEMA_PATH = ProjectConfigManager.getInstance().getProperty("sbs.schema.path");
    public static final String SCHEMA_PREFIX = ProjectConfigManager.getInstance().getProperty("sbs.schema.prefix");
    public static final String SCHEMA_SUFFIX = ProjectConfigManager.getInstance().getProperty("sbs.schema.suffix");
    public static final String SCHEMA_EXTENSION = ProjectConfigManager.getInstance().getProperty("sbs.schema.extension");

    public static Schema getSchema(String schemaFileName) {
        if (StringUtils.isEmpty(schemaFileName)) {
            return null;
        } else if (!schemaFileName.contains(SCHEMA_SUFFIX)) {
            schemaFileName = schemaFileName + SCHEMA_SUFFIX;
        }
        schemaFileName = (SCHEMA_PATH + schemaFileName).trim();
        logger.info("��ʼ��ȡSchema�ļ���" + schemaFileName);
        InputStream inputStream = AvroSchemaManager.class.getClassLoader().getResourceAsStream(schemaFileName);
        try {
            Schema schema = new Schema.Parser().parse(inputStream);
            return schema;
        } catch (IOException e) {
            logger.info("Schema�ļ���ȡ����", e);
            throw new RuntimeException(e);
        }
    }

}
