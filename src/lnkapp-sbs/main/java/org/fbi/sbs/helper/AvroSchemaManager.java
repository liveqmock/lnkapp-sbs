package org.fbi.sbs.helper;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 模式文件管理
 */
public class AvroSchemaManager {
    public static final Logger logger = LoggerFactory.getLogger(ProjectConfigManager.class);

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
}
