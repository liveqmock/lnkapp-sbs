package org.fbi.sbs.online.code.txn88481.processor;

import org.apache.ibatis.session.SqlSession;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.sbs.enums.TxnRtnCode;
import org.fbi.sbs.helper.MybatisFactory;
import org.fbi.sbs.helper.ProjectConfigManager;
import org.fbi.sbs.internal.Processor;
import org.fbi.sbs.online.code.txn88481.domain.IndustryInfo;
import org.fbi.sbs.online.code.txn88481.domain.Tia88481;
import org.fbi.sbs.online.code.txn88481.domain.Toa88481;
import org.fbi.sbs.processor.AbstractTxnProcessor;
import org.fbi.sbs.repository.dao.ActtscMapper;
import org.fbi.sbs.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Processor(txnCode = "88481")
public class T88481Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {

        Tia88481 tia = initTia(request, response);

        // ��ȡ�����������ԱID������
        String tellerId = request.getHeader("tellerId").trim().substring(0, 4);
        int begnum = Integer.parseInt(tia.getBegnum().toString());
        if (begnum == 0) {
            begnum = 1;
        }

        logger.info("88481��ҵ�����ʲ�ѯ������Ա��" + tellerId + " ��ʼ�룺" + begnum);
        List<Acttsc> tscList = qryActtscs(begnum);


        Toa88481 toa = new Toa88481();
        toa.setTotcnt(String.valueOf(tscList.size()));
        List<IndustryInfo> infos = new ArrayList<>();
        for (Acttsc record : tscList) {
            IndustryInfo info = new IndustryInfo();
            info.setTddtdc(record.getTddtdc());
            info.setTddnam(record.getTddnam());
            infos.add(info);
        }
        toa.setInfos(infos);
        response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
        try {
            response.setResponseBody(encode(toa.getSchema(), toa, response.getCharacterEncoding()));
        } catch (Exception e) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(("��װ��Ӧ����ʱ���л��쳣").getBytes(response.getCharacterEncoding()));
            logger.error("��ɲ�ѯ����װ��Ӧ����ʱ���л��쳣", e);
        }
    }

    private Tia88481 initTia(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws UnsupportedEncodingException {
        Tia88481 tia = null;
        try {
            tia = (Tia88481) decode(Tia88481.class, request.getRequestBody(), request.getCharacterEncoding());
        } catch (Exception e) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(("������������쳣").getBytes(response.getCharacterEncoding()));
            logger.error("������������쳣", e);
            throw new RuntimeException("������������쳣");
        }
        return tia;
    }

    // ��ʲ�ѯ,ÿ��������������:sbs.list.size
    private List<Acttsc> qryActtscs(int begnum) {
        int cnt = Integer.parseInt(ProjectConfigManager.getInstance().getProperty("sbs.list.size"));
        int endIndex = begnum + cnt - 1;
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtscMapper mapper = session.getMapper(ActtscMapper.class);
        return mapper.qryByIndex(String.valueOf(begnum), String.valueOf(endIndex));
    }


}
