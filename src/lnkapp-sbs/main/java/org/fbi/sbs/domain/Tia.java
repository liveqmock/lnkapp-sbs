package org.fbi.sbs.domain;

import org.apache.avro.generic.GenericData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Created by lenovo on 2014-4-10 0010.
 */
public class Tia {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void from(GenericData.Record record) throws IllegalAccessException {

        Class clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            // TODO 暂时只处理String类型
            f.set(this, (record.get(f.getName()).toString()).trim());
        }

        logger.info("Request bean 装配结束.");
    }
}
