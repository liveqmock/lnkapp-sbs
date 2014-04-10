package org.fbi.sbs.processor;

import org.apache.avro.generic.GenericData;
import org.apache.commons.lang.StringUtils;
import org.fbi.linking.processor.Processor;
import org.fbi.sbs.domain.AssembleM;
import org.fbi.sbs.enums.TxnRtnCode;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10Processor;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.sbs.helper.AvroSchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * User: zhanrui
 * Date: 2014-3-17
 * Merge:zhangxiaobo
 */
public abstract class AbstractTxnProcessor extends Stdp10Processor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected AssembleM m;

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
            logger.info("FEB Request:" + request.toString());
            // 得到包体bean
            assembleRequestBean(txnCode, request.getRequestBody());
            //
            doRequest(request, response);
            logger.info("FEB Response:" + response.toString());
        } catch (Exception e) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            throw new RuntimeException(e);
        } finally {
            MDC.remove("txnCode");
            MDC.remove("tellerId");
        }
    }

    private void assembleRequestBean(String txnCode, byte[] requestBody) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {

        String[] names = this.getClass().getPackage().getName().split("\\.");
        String className = names[0] + "." + names[1] + "." + names[2] + ".domain.M" + txnCode;
        Class clazz = Class.forName(className);
        m = (AssembleM)clazz.newInstance();

        String strRequestBody = new String(requestBody);
        logger.info(" Request Schema:[" + txnCode + "]" + strRequestBody);
        GenericData.Record record = AvroSchemaManager.getSchemaObj(txnCode, strRequestBody);
        m.assemble(record);
    }

    abstract protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException;

}
