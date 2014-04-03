package org.fbi.sbs.processor;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.sbs.enums.TxnRtnCode;
import org.fbi.sbs.helper.AvroSchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class T8848Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {

        String serialStr = new String(request.getRequestBody());
        logger.info("Schema:" + serialStr);

        Schema schema = AvroSchemaManager.getSchema("schemas/M8848.avsc");
        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, serialStr);
        GenericData.Record m8848 = new GenericData.Record(schema);
        datumReader.read(m8848, decoder);
        /*
        {"name": "batseq", "type": "string"},
     {"name": "orgidt", "type": "string"},
     {"name": "depnum", "type": "string"},
     {"name": "pastyp", "type": "string"},
     {"name": "inpflg", "type": "string"},
     {"name": "sbknum", "type": "string"},
     {"name": "wrkunt", "type": "string"},
     {"name": "stmadd", "type": "string"},
     {"name": "intnet", "type": "string"},

     {"name": "engnam", "type": "string"},
     {"name": "regadd", "type": "string"},
     {"name": "coradd", "type": "string"},
     {"name": "cusnam", "type": "string"},
     {"name": "funcde", "type": "string"},
     {"name": "begnum", "type": "string"}
         */


        response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
        response.setResponseBody("交易完成".getBytes(response.getCharacterEncoding()));
    }
}
