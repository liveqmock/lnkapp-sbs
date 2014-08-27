package org.fbi.sbs.online.code.txn88480.processor;

import org.apache.ibatis.session.SqlSession;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.sbs.enums.TxnRtnCode;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;

@Processor(txnCode = "88480")
public class T88480Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {

        Tia88480 tia = initTia(request, response);

        // 获取请求操作的人员ID和日期
        String tellerId = request.getHeader("tellerId").trim().substring(0, 4);

        Acttsc tsc = new Acttsc();
        Acttmc tmc = new Acttmc();
        Acttbc tbc = new Acttbc();
        String pastyp = tia.getPastyp().toString();
        String inpflg = tia.getInpflg().toString();
        String sbknum = tia.getSbknum().toString();
        tmc.setTdbsrt(pastyp);
        tmc.setTdmsrt(inpflg);
        tbc.setTdbsrt(pastyp);
        tsc.setTddtdc(pastyp + inpflg + sbknum);
        logger.info("88480行业代码单笔查询，操作员：" + tellerId + " 查询码：" + pastyp + inpflg + sbknum);

        // 单笔查询
        tsc = qryActtscByTddtdc(tsc.getTddtdc());
        tbc.setTdbnam(qryTdbnamByTdbsrt(tbc.getTdbsrt()));
        tmc.setTdmnam(qryTdmnamByTdbTdmsrt(tmc.getTdbsrt(), tmc.getTdmsrt()));

        Toa88480 toa = new Toa88480();

        toa.setTdbsrt(tbc.getTdbsrt());
        toa.setTdmsrt(tmc.getTdmsrt());
        toa.setTdssrt(tsc.getTddtdc().substring(3));
        toa.setTdmnam(tmc.getTdmnam());
        toa.setTdbnam(tbc.getTdbnam());
        toa.setTdsnam(tsc.getTddnam());  // tdsnam = tddnam
        toa.setTddnam(tsc.getTddnam());
        toa.setTddcn1(tsc.getTddcn1());
        toa.setTddcn2(tsc.getTddcn2());
        toa.setTddcn3(tsc.getTddcn3());
        toa.setTddcn4(tsc.getTddcn4());
        toa.setAmdtlr(tsc.getAmdtlr());
        toa.setUpddat(new SimpleDateFormat("yyyyMMdd HH:mm:dd").format(tsc.getUpddat()));
        response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
        try {
            response.setResponseBody(encode(toa.getSchema(), toa, response.getCharacterEncoding()));
        } catch (Exception e) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(("组装响应报文时序列化异常").getBytes(response.getCharacterEncoding()));
            logger.error("完成查询，组装响应报文时序列化异常", e);
        }
    }

    private Tia88480 initTia(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws UnsupportedEncodingException {
        Tia88480 tia = null;
        try {
            tia = (Tia88480) decode(Tia88480.class, request.getRequestBody(), request.getCharacterEncoding());
        } catch (Exception e) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(("请求报文体解码异常").getBytes(response.getCharacterEncoding()));
            logger.error("请求报文体解码异常", e);
            throw new RuntimeException("请求报文体解码异常");
        }
        return tia;
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
