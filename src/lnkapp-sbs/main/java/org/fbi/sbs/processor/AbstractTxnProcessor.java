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
import org.fbi.sbs.helper.ProjectConfigManager;
import org.fbi.sbs.online.code.txn88480.domain.Toa88480;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

/**
 * User: zhanrui
 * Date: 2014-3-17
 * Merge:zhangxiaobo
 */
public abstract class AbstractTxnProcessor extends Stdp10Processor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String CODING_TYPE = ProjectConfigManager.getInstance().getProperty("sbs.avro.coding");

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

    // 解码
    protected Object decode(Class txnBeanClass, byte[] buf, String charsetName) throws IllegalAccessException, InstantiationException, IOException {
        Object obj = txnBeanClass.newInstance();
        SpecificDatumReader datumReader = new SpecificDatumReader(txnBeanClass);
        Decoder decoder = null;
        if ("json".equalsIgnoreCase(CODING_TYPE)) {
            decoder = DecoderFactory.get().jsonDecoder(datumReader.getSchema(), new String(buf, charsetName));
        } else if ("binary".equalsIgnoreCase(CODING_TYPE)) {
            decoder = DecoderFactory.get().binaryDecoder(buf, null);
        } else {
            throw new RuntimeException("无效 AVRO 编码方式：" + CODING_TYPE);
        }
        datumReader.read(obj, decoder);
        return obj;
    }

    // 编码
    protected byte[] encode(Schema schema, Object toa, String charsetName) throws IOException, IllegalAccessException {
        SpecificDatumWriter datumWriter = new SpecificDatumWriter(schema);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Encoder encoder = null;
        if ("json".equalsIgnoreCase(CODING_TYPE)) {
            encoder = EncoderFactory.get().jsonEncoder(schema, baos);
        } else if ("binary".equalsIgnoreCase(CODING_TYPE)) {
            encoder = EncoderFactory.get().binaryEncoder(baos, null);
        } else {
            throw new RuntimeException("无效 AVRO 编码方式：" + CODING_TYPE);
        }
        datumWriter.write(toa, encoder);
        encoder.flush();
        baos.close();
        String res = baos.toString(charsetName);
        return res.getBytes(charsetName);
    }
}
