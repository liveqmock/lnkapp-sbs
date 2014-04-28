package org.fbi.sbs.processor;

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
import org.fbi.sbs.enums.TxnRtnCode;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10Processor;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.sbs.helper.AvroSchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    // µ¥±Ê½âÂë
    protected Tia decode(String txnCode, byte[] buf) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Schema schema = AvroSchemaManager.getSchema(AvroSchemaManager.SCHEMA_PREFIX + txnCode);
        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, new String(buf, "UTF-8"));

        GenericData.Record record = new GenericData.Record(schema);
        datumReader.read(record, decoder);

        String className = "org.fbi.sbs.domain.tia.Tia" + txnCode;
        Class clazz = Class.forName(className);
        Tia tia = (Tia) clazz.newInstance();
        tia.from(record);
        return tia;
    }

    // µ¥±Ê±àÂë
    protected byte[] encode(String formCode, Toa toa) throws IOException, IllegalAccessException {
        Schema schema = AvroSchemaManager.getSchema(formCode);
        GenericDatumWriter<GenericData.Record> datumWriter = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, baos);
        GenericData.Record record = new GenericData.Record(schema);
        toa.to(record);
        datumWriter.write(record, encoder);
        encoder.flush();
        baos.close();
        return baos.toByteArray();
    }
}
