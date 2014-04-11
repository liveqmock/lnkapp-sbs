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

        // ��ȡ�����������ԱID������
        String tellerId = request.getHeader("tellerId").trim().substring(0, 4);
        Date today = new Date();

        // Acttsc��ҵ����С��� Acttmc ��ҵ��������� Acttbc��ҵ��������
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

        // ���ݹ������ʼ������
        // 4-�� 3-ɾ 2-��
        int funcde = Integer.parseInt(tia8848.getFuncde());
        if (funcde < 0 || funcde > 4) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(("������[" + funcde + "]����").getBytes(response.getCharacterEncoding()));
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
            // ����begnum��ACTTSC[��ҵ����С��]��¼
            begnum = Integer.parseInt(tia8848.begnum);
        }
        List<Acttsc> tscList = qryAllActtsc();

        switch (funcde) {
            case 0:
                // ���ʲ�ѯ
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
                    response.setResponseBody(("��װ��Ӧ����ʱ���л��쳣").getBytes(response.getCharacterEncoding()));
                    logger.error("��ɲ�ѯ����װ��Ӧ����ʱ���л��쳣", e);
                }
                break;
            case 1:
                // ����begnum�ʼ�¼
                // TODO ���
                tscList = tscList.subList(begnum, tscList.size() - 1);
                break;
            case 2:
                // ������ҵ��������
                if (!updateActtc(tsc, tmc, tbc)) {
                    response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
                    response.setResponseBody(("��ҵ�������ʧ��").getBytes(response.getCharacterEncoding()));
                }
                break;
            case 3:
                // ɾ����ҵ����
                if (!deleteActtc(tsc, tmc)) {
                    response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
                    response.setResponseBody(("��ҵ����ɾ��ʧ��").getBytes(response.getCharacterEncoding()));
                }
                break;
            case 4:
                // ������ҵ���� 0-�ɹ� 1-�ظ�д�� -1-�쳣������ع�
                int cnt = insertActtc(tsc, tmc, tbc);
                if (1 == cnt) {
                    response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
                    response.setResponseBody(("��ҵ����" + tsc.getTddtdc() + "�Ѵ���").getBytes(response.getCharacterEncoding()));
                } else if (-1 == cnt) {
                    response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
                    response.setResponseBody(("��ҵ�������ʧ��").getBytes(response.getCharacterEncoding()));
                } else {
                    // ���׳ɹ�
                }
                break;
        }

        if (StringUtils.isEmpty(response.getHeader("rtnCode"))) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
            response.setResponseBody("�������".getBytes(response.getCharacterEncoding()));
        }
    }

    private List<Acttsc> qryAllActtsc() {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtscMapper mapper = session.getMapper(ActtscMapper.class);
        ActtscExample example = new ActtscExample();
        example.setOrderByClause(" TDDTDC desc ");
        return mapper.selectByExample(example);
    }

    // ����С�����ѯС��
    private Acttsc qryActtscByTddtdc(String tddtdc) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtscMapper mapper = session.getMapper(ActtscMapper.class);
        ActtscExample example = new ActtscExample();
        example.createCriteria().andTddtdcEqualTo(tddtdc);
        List<Acttsc> records = mapper.selectByExample(example);
        return records.size() > 0 ? records.get(0) : null;
    }

    // ���ݴ������ѯ��������
    private String qryTdbnamByTdbsrt(String tdbsrt) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtbcMapper mapper = session.getMapper(ActtbcMapper.class);
        ActtbcExample example = new ActtbcExample();
        example.createCriteria().andTdbsrtEqualTo(tdbsrt);
        List<Acttbc> records = mapper.selectByExample(example);
        return records.size() > 0 ? records.get(0).getTdbnam() : null;
    }

    // ���ݴ�����+�������ѯ��������
    private String qryTdmnamByTdbTdmsrt(String tdbsrt, String tdmsrt) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtmcMapper mapper = session.getMapper(ActtmcMapper.class);
        ActtmcExample example = new ActtmcExample();
        example.createCriteria().andTdbsrtEqualTo(tdbsrt).andTdmsrtEqualTo(tdmsrt);
        List<Acttmc> records = mapper.selectByExample(example);
        return records.size() > 0 ? records.get(0).getTdmnam() : null;
    }

    // ������ҵ��������
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
            logger.error("������ҵ�����¼�쳣", e);
            session.rollback();
            return false;
        } finally {
            if (session != null) {
                session.close();
                session = null;
            }
        }
    }

    // ɾ����ҵ����
    private boolean deleteActtc(Acttsc tsc, Acttmc tmc) {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        try {
            // ɾ����ҵС�࣬��ͬ��ҵ����������ҵС�࣬��ɾ����ҵ���࣬��ͬ��ҵ������������ҵ���࣬��ɾ����ҵ����
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
            logger.error("ɾ����ҵ�����¼�쳣", e);
            session.rollback();
            return false;
        } finally {
            if (session != null) {
                session.close();
                session = null;
            }
        }
    }

    // ������ҵ���� 0-�ɹ� 1-�ظ�д�� -1-�쳣������ع�
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
            logger.error("������ҵ�����¼�쳣", e);
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
