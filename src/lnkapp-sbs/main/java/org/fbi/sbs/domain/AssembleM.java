package org.fbi.sbs.domain;

import org.apache.avro.generic.GenericData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Created by lenovo on 2014-4-10 0010.
 */
public class AssembleM {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void assemble(GenericData.Record record) throws IllegalAccessException {

        Class clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for(Field f : fields) {
            f.setAccessible(true);
            // TODO ��ʱֻ����String����
            f.set(this, ((String) (record.get(f.getName()))).trim());
        }

        logger.info("Request bean װ�����.");
    }
}
