package org.fbi.sbs.domain;

import org.apache.avro.generic.GenericData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Created by lenovo on 2014-4-10 0010.
 */
public class Toa {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void to(GenericData.Record record) throws IllegalAccessException {
        Class clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            // TODO 暂时只处理String类型
            Object val = f.get(this);
            if (val == null) {
                val = "";
            }
            record.put(f.getName().toLowerCase().trim(), val);
        }
        logger.info("Toa GenericData.Record 装配结束.");
    }
}
