package org.fbi.sbs.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.sbs.domain.Tia8848;
import org.fbi.sbs.domain.ToaT908;
import org.fbi.sbs.enums.TxnRtnCode;
import org.fbi.sbs.helper.AvroSchemaManager;
import org.fbi.sbs.helper.FbiBeanUtils;
import org.fbi.sbs.helper.MybatisFactory;
import org.fbi.sbs.repository.dao.ActtbcMapper;
import org.fbi.sbs.repository.dao.ActtmcMapper;
import org.fbi.sbs.repository.dao.ActtscMapper;
import org.fbi.sbs.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class T8848Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {

        Tia8848 tia8848 = (Tia8848) tia;

        // 获取请求操作的人员ID和日期
        String tellerId = request.getHeader("tellerId").trim().substring(0, 4);
        Date today = new Date();

        // Acttsc行业代码小类表 Acttmc 行业代码中类表 Acttbc行业代码大类表
        Acttsc tsc = new Acttsc();
        Acttmc tmc = new Acttmc();
        Acttbc tbc = new Acttbc();
        tsc.setAmdtlr(tellerId);
        tsc.setUpddat(today);
        tmc.setAmdtlr(tellerId);
        tmc.setUpddat(today);
        tbc.setAmdtlr(tellerId);
        tbc.setUpddat(today);

        int begnum = 0;

        // 根据功能码初始化变量
        // 4-增 3-删 2-改
        int funcde = Integer.parseInt(tia8848.getFuncde());
        if (funcde < 0 || funcde > 4) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(("功能码[" + funcde + "]错误").getBytes(response.getCharacterEncoding()));
            return;
        } else if (funcde == 0 || funcde == 3) {
            tbc.setTdbsrt(tia8848.pastyp);
            tmc.setTdbsrt(tia8848.pastyp);
            tmc.setTdmsrt(tia8848.inpflg);
            tsc.setTddtdc(tia8848.pastyp + tia8848.inpflg + tia8848.sbknum);
        } else if (funcde == 2 || funcde == 4) {
            tbc.setTdbsrt(tia8848.pastyp);
            tmc.setTdbsrt(tia8848.pastyp);
            tmc.setTdmsrt(tia8848.inpflg);
            tsc.setTddtdc(tia8848.pastyp + tia8848.inpflg + tia8848.sbknum);
            tbc.setTdbnam(tia8848.wrkunt);
            tmc.setTdmnam(tia8848.stmadd);
            tsc.setTddnam(tia8848.intnet);
            tsc.setTddcn1(tia8848.engnam);
            tsc.setTddcn2(tia8848.regadd);
            tsc.setTddcn3(tia8848.coradd);
            tsc.setTddcn4(tia8848.cusnam);
        } else if (funcde == 1) {
            // 跳过begnum笔ACTTSC[行业代码小类]记录
            begnum = Integer.parseInt(tia8848.begnum);
        }
        List<Acttsc> tscList = qryAllActtsc();

        switch (funcde) {
            case 0:
                // 单笔查询
                tsc = qryActtscByTddtdc(tsc.getTddtdc());
                tbc.setTdbnam(qryTdbnamByTdbsrt(tbc.getTdbsrt()));
                tmc.setTdmnam(qryTdmnamByTdbTdmsrt(tmc.getTdbsrt(), tmc.getTdmsrt()));
                ToaT908 t908 = new ToaT908();
                FbiBeanUtils.copyProperties(tsc, t908);
                t908.tdbsrt = tbc.getTdbsrt();
                t908.tdmsrt = tmc.getTdmsrt();
                t908.tdssrt = tsc.getTddtdc().substring(3);
                t908.tdmnam = tmc.getTdmnam();
                t908.tdbnam = tbc.getTdbnam();
                response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
                try {
                    response.setResponseBody(AvroSchemaManager.encode("T908", t908));
                } catch (IllegalAccessException e) {
                    response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
                    response.setResponseBody(("组装响应报文时序列化异常").getBytes(response.getCharacterEncoding()));
                    logger.error("完成查询，组装响应报文时序列化异常", e);
                }
                break;
            case 1:
                // 跳过begnum笔记录
                // TODO 多笔
                tscList = tscList.subList(begnum, tscList.size() - 1);
                break;
            case 2:
                // 更新行业代码三表
                if (!updateActtc(tsc, tmc, tbc)) {
                    response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
                    response.setResponseBody(("行业代码更新失败").getBytes(response.getCharacterEncoding()));
                }
                break;
            case 3:
                // 删除行业代码
                if (!deleteActtc(tsc, tmc)) {
                    response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
                    response.setResponseBody(("行业代码删除失败").getBytes(response.getCharacterEncoding()));
                }
                break;
            case 4:
                // 增加行业代码 0-成功 1-重复写入 -1-异常，事务回滚
                int cnt = insertActtc(tsc, tmc, tbc);
                if (1 == cnt) {
                    response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
                    response.setResponseBody(("行业代码" + tsc.getTddtdc() + "已存在").getBytes(response.getCharacterEncoding()));
                } else if (-1 == cnt) {
                    response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
                    response.setResponseBody(("行业代码添加失败").getBytes(response.getCharacterEncoding()));
                } else {
                    // 交易成功
                }
                break;
        }

        if (StringUtils.isEmpty(response.getHeader("rtnCode"))) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
            response.setResponseBody("交易完成".getBytes(response.getCharacterEncoding()));
        }
    }

    private List<Acttsc> qryAllActtsc() {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtscMapper mapper = session.getMapper(ActtscMapper.class);
        ActtscExample example = new ActtscExample();
        example.setOrderByClause(" TDDTDC desc ");
        return mapper.selectByExample(example);
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

    // 更新行业代码三表
    private boolean updateActtc(Acttsc tsc, Acttmc tmc, Acttbc tbc) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        try {
            ActtscMapper tscMapper = session.getMapper(ActtscMapper.class);
            ActtscExample tscExample = new ActtscExample();
            tscExample.createCriteria().andTddtdcEqualTo(tsc.getTddtdc());
            tscMapper.updateByExample(tsc, tscExample);

            ActtmcMapper tmcMapper = session.getMapper(ActtmcMapper.class);
            ActtmcExample tmcExample = new ActtmcExample();
            tmcExample.createCriteria().andTdbsrtEqualTo(tmc.getTdbsrt()).andTdmsrtEqualTo(tmc.getTdmsrt());
            tmcMapper.updateByExample(tmc, tmcExample);

            ActtbcMapper tbcMapper = session.getMapper(ActtbcMapper.class);
            ActtbcExample tbcExample = new ActtbcExample();
            tbcExample.createCriteria().andTdbsrtEqualTo(tbc.getTdbsrt());
            tbcMapper.updateByExample(tbc, tbcExample);

            session.commit();
            return true;
        } catch (Exception e) {
            logger.error("更新行业代码记录异常", e);
            session.rollback();
            return false;
        } finally {
            if (session != null) {
                session.close();
                session = null;
            }
        }
    }

    // 删除行业代码
    private boolean deleteActtc(Acttsc tsc, Acttmc tmc) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        try {
            // 删除行业小类，若同行业中类已无行业小类，则删除行业中类，若同行业大类中已无行业中类，则删除行业大类
            ActtscMapper tscMapper = session.getMapper(ActtscMapper.class);
            ActtscExample tscExample = new ActtscExample();
            tscExample.createCriteria().andTddtdcEqualTo(tsc.getTddtdc());
            tscMapper.deleteByExample(tscExample);
            if (0 == tscMapper.countSameTmc(tmc.getTdbsrt(), tmc.getTdmsrt())) {
                ActtmcMapper tmcMapper = session.getMapper(ActtmcMapper.class);
                ActtmcExample tmcExample = new ActtmcExample();
                tmcExample.createCriteria().andTdbsrtEqualTo(tmc.getTdbsrt()).andTdmsrtEqualTo(tmc.getTdmsrt());
                tmcMapper.deleteByExample(tmcExample);
                if (0 == tmcMapper.countSameTbc(tmc.getTdbsrt())) {
                    ActtbcMapper tbcMapper = session.getMapper(ActtbcMapper.class);
                    ActtbcExample tbcExample = new ActtbcExample();
                    tbcExample.createCriteria().andTdbsrtEqualTo(tmc.getTdbsrt());
                    tbcMapper.deleteByExample(tbcExample);
                }
            }
            session.commit();
            return true;
        } catch (Exception e) {
            logger.error("删除行业代码记录异常", e);
            session.rollback();
            return false;
        } finally {
            if (session != null) {
                session.close();
                session = null;
            }
        }
    }

    // 增加行业代码 0-成功 1-重复写入 -1-异常，事务回滚
    private int insertActtc(Acttsc tsc, Acttmc tmc, Acttbc tbc) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        try {
            //
            ActtscMapper tscMapper = session.getMapper(ActtscMapper.class);
            ActtscExample tscExample = new ActtscExample();
            tscExample.createCriteria().andTddtdcEqualTo(tsc.getTddtdc());
            int cnt = tscMapper.countByExample(tscExample);
            if (0 == cnt) {
                tscMapper.insert(tsc);
                ActtmcMapper tmcMapper = session.getMapper(ActtmcMapper.class);
                ActtmcExample tmcExample = new ActtmcExample();
                tmcExample.createCriteria().andTdbsrtEqualTo(tmc.getTdbsrt()).andTdmsrtEqualTo(tmc.getTdmsrt());
                if (0 == tmcMapper.countByExample(tmcExample)) {
                    tmcMapper.insert(tmc);
                } else {
                    tmcMapper.updateByExample(tmc, tmcExample);
                }
                ActtbcMapper tbcMapper = session.getMapper(ActtbcMapper.class);
                ActtbcExample tbcExample = new ActtbcExample();
                tbcExample.createCriteria().andTdbsrtEqualTo(tbc.getTdbsrt());
                if (0 == tbcMapper.countByExample(tbcExample)) {
                    tbcMapper.insert(tbc);
                } else {
                    tbcMapper.updateByExample(tbc, tbcExample);
                }
                session.commit();
                return 0;
            } else {
                return 1;
            }
        } catch (Exception e) {
            logger.error("新增行业代码记录异常", e);
            session.rollback();
            return -1;
        } finally {
            if (session != null) {
                session.close();
                session = null;
            }
        }
    }

}
