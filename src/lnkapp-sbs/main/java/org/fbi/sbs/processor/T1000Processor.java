package org.fbi.sbs.processor;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.fbi.sbs.enums.TxnRtnCode;
import org.fbi.sbs.helper.AvroSchemaManager;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class T1000Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {

        String serialStr = new String(request.getRequestBody());
        logger.info("Schema:" + serialStr);

        Schema schema = AvroSchemaManager.getSchema("schemas/user.avsc");
        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, serialStr);
        GenericData.Record user = new GenericData.Record(schema);
        datumReader.read(user, decoder);
        logger.info("User name: " + user.get("name"));
        logger.info("User favorite_number: " + user.get("favorite_number"));
        logger.info("User favorite_color: " + user.get("favorite_color"));

        response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
        response.setResponseBody("交易完成".getBytes(response.getCharacterEncoding()));
    }
}
