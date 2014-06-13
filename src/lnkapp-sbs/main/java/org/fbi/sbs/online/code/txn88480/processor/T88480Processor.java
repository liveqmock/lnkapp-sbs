package org.fbi.sbs.online.code.txn88480.processor;

import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.ibatis.session.SqlSession;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.sbs.enums.TxnRtnCode;
import org.fbi.sbs.helper.AvroSchemaManager;
import org.fbi.sbs.helper.FbiBeanUtils;
import org.fbi.sbs.helper.MybatisFactory;
import org.fbi.sbs.internal.Processor;
import org.fbi.sbs.online.code.txn88480.domain.Tia88480;
import org.fbi.sbs.online.code.txn88480.domain.Toa88480;
import org.fbi.sbs.processor.AbstractTxnProcessor;
import org.fbi.sbs.repository.dao.ActtbcMapper;
import org.fbi.sbs.repository.dao.ActtmcMapper;
import org.fbi.sbs.repository.dao.ActtscMapper;
import org.fbi.sbs.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Processor(txnCode = "88480")
public class T88480Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {

       /* Tia88480 tia = new Tia88480();
        SpecificDatumReader<Tia88480> datumReader = new SpecificDatumReader<Tia88480>(Tia88480.class);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(datumReader.getSchema(), new String(request.getRequestBody(), "UTF-8"));
        datumReader.read(tia, decoder);*/

        Tia88480 tia = null;
        try {
            tia = (Tia88480) decode(Tia88480.class, request.getRequestBody());
        } catch (Exception e) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(("请求报文体解码异常").getBytes(response.getCharacterEncoding()));
            logger.error("请求报文体解码异常", e);
            return;
        }

        // 获取请求操作的人员ID和日期
        String tellerId = request.getHeader("tellerId").trim().substring(0, 4);
        logger.info("88480行业代码单笔查询，操作员：" + tellerId);

        Acttsc tsc = new Acttsc();
        Acttmc tmc = new Acttmc();
        Acttbc tbc = new Acttbc();
        String pastyp = tia.getPastyp().toString();
        String inpflg = tia.getInpflg().toString();
        String sbknum = tia.getSbknum().toString();
        tmc.setTdbsrt(pastyp);
        tmc.setTdmsrt(inpflg);
        tsc.setTddtdc(pastyp + inpflg + sbknum);

        // 单笔查询
        tsc = qryActtscByTddtdc(tsc.getTddtdc());
        tbc.setTdbnam(qryTdbnamByTdbsrt(tbc.getTdbsrt()));
        tmc.setTdmnam(qryTdmnamByTdbTdmsrt(tmc.getTdbsrt(), tmc.getTdmsrt()));
        Toa88480 toa = new Toa88480();
        FbiBeanUtils.copyProperties(tsc, toa);
        toa.setTdbsrt(tbc.getTdbsrt());
        toa.setTdmsrt(tmc.getTdmsrt());
        toa.setTdssrt(tsc.getTddtdc().substring(3));
        toa.setTdmnam(tmc.getTdmnam());
        toa.setTdbnam(tbc.getTdbnam());
        response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
        try {
           /* SpecificDatumWriter<Toa88480> datumWriter = new SpecificDatumWriter<>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Encoder encoder = EncoderFactory.get().jsonEncoder(toa.getSchema(), baos);
            datumWriter.write(toa, encoder);
            response.setResponseBody(baos.toByteArray());*/
            response.setResponseBody(encode(toa.getSchema(), toa));
        } catch (Exception e) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(("组装响应报文时序列化异常").getBytes(response.getCharacterEncoding()));
            logger.error("完成查询，组装响应报文时序列化异常", e);
        }
    }

    // 根据小类码查询小类
    private Acttsc qryActtscByTddtdc(String tddtdc) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtscMapper mapper = session.getMapper(ActtscMapper.class);
        ActtscExample example = new ActtscExample();
        example.createCriteria().andTddtdcEqualTo(tddtdc);
        List<Acttsc> records = mapper.selectByExample(example);
        return records.size() > 0 ? records.get(0) : null;
    }

    // 根据大类码查询大类名称
    private String qryTdbnamByTdbsrt(String tdbsrt) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtbcMapper mapper = session.getMapper(ActtbcMapper.class);
        ActtbcExample example = new ActtbcExample();
        example.createCriteria().andTdbsrtEqualTo(tdbsrt);
        List<Acttbc> records = mapper.selectByExample(example);
        return records.size() > 0 ? records.get(0).getTdbnam() : null;
    }

    // 根据大类码+中类码查询中类名称
    private String qryTdmnamByTdbTdmsrt(String tdbsrt, String tdmsrt) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtmcMapper mapper = session.getMapper(ActtmcMapper.class);
        ActtmcExample example = new ActtmcExample();
        example.createCriteria().andTdbsrtEqualTo(tdbsrt).andTdmsrtEqualTo(tdmsrt);
        List<Acttmc> records = mapper.selectByExample(example);
        return records.size() > 0 ? records.get(0).getTdmnam() : null;
    }

}
