package org.fbi.sbs.domain;

import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

/**
 * Created by lenovo on 2014-4-10 0010.
 */
public class Tia {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void from(GenericData.Record record) throws IllegalAccessException, UnsupportedEncodingException {

        Class clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            // TODO 暂时只处理String类型 中文乱码
            Object obj = record.get(f.getName());
            if (obj instanceof Utf8) {
//                String val = new String(obj.toString().getBytes(), "GBK");
                String val = obj.toString();
                f.set(this, val.trim());
            } else {
                throw new RuntimeException("交易暂时只支持字符类型");
            }
        }
    }
}
