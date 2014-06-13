package org.fbi.sbs.processor;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.lang.StringUtils;
import org.fbi.sbs.enums.TxnRtnCode;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10Processor;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.sbs.helper.AvroSchemaManager;
import org.fbi.sbs.online.code.txn88480.domain.Toa88480;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * User: zhanrui
 * Date: 2014-3-17
 * Merge:zhangxiaobo
 */
public abstract class AbstractTxnProcessor extends Stdp10Processor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String txnCode = request.getHeader("txnCode");
        String tellerId = request.getHeader("tellerId");
        if (StringUtils.isEmpty(tellerId)) {
            tellerId = "TELLERID";
        }

        try {
            MDC.put("txnCode", txnCode);
            MDC.put("tellerId", tellerId);
            doRequest(request, response);
        } catch (Exception e) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(TxnRtnCode.TXN_EXECUTE_FAILED.getTitle().getBytes(response.getCharacterEncoding()));
            throw new RuntimeException(e);
        } finally {
            MDC.remove("txnCode");
            MDC.remove("tellerId");
        }
    }


    abstract protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException;

    // ½âÂë
    protected Object decode(Class txnBeanClass, byte[] buf) throws IllegalAccessException, InstantiationException, IOException {
        Object obj = txnBeanClass.newInstance();
        SpecificDatumReader<Object> datumReader = new SpecificDatumReader<Object>(txnBeanClass);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(datumReader.getSchema(), new String(buf, "UTF-8"));
        datumReader.read(obj, decoder);
        return obj;
    }

    // ±àÂë
    protected byte[] encode(Schema schema, Object toa) throws IOException, IllegalAccessException {
        SpecificDatumWriter<Object> datumWriter = new SpecificDatumWriter<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().jsonEncoder(schema, baos);
        datumWriter.write(toa, encoder);
        encoder.flush();
        baos.close();
        return baos.toByteArray();
    }
}
