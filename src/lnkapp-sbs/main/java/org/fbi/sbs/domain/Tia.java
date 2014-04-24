package org.fbi.sbs.domain;

import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.apache.commons.lang.StringUtils;
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
                Utf8 utfVal = (Utf8) obj;
//                String val = new String(utfVal.toString().getBytes("GBK"), "GBK");
//                logger.info(getHexString(utfVal.getBytes()) + "::Utf8::" + utfVal.toString() + "::GBK::" + val);
                f.set(this, utfVal.toString());

            } else {
                throw new RuntimeException("交易暂时只支持字符类型");
            }
        }
    }

    public String getHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
//            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
            result += Integer.toHexString(b[i] & 0xff);
        }
        return result;
    }

}
