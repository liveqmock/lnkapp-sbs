package org.fbi.sbs.service;


import org.fbi.linking.processor.Processor;
import org.fbi.linking.processor.ProcessorManagerService;
import org.fbi.sbs.internal.ProcessorScanner;
import org.fbi.sbs.processor.AbstractTxnProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Hashtable;

/**
 * User: zhangxiaobo
 */
public class ProcessorManagerServiceImpl implements ProcessorManagerService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Hashtable<String, AbstractTxnProcessor> processors;

    public ProcessorManagerServiceImpl() {
        init();
    }

    public void init() {
        try {
            this.processors = new ProcessorScanner().scan("org.fbi.sbs.online");
        } catch (Exception e) {
            logger.error("Processor扫描异常.", e);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    public Processor getProcessor(String txnCode) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        AbstractTxnProcessor processor = processors.get(txnCode);
        if (processor == null) {
            init();
            processor = processors.get(txnCode);
        }
        if (processor == null) {
            throw new RuntimeException("SBS不存在交易" + txnCode);
        }
        return processor;
    }
}
