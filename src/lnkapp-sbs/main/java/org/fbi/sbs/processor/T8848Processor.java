package org.fbi.sbs.processor;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.ibatis.session.SqlSession;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.sbs.domain.M8848;
import org.fbi.sbs.enums.TxnRtnCode;
import org.fbi.sbs.helper.AvroSchemaManager;
import org.fbi.sbs.helper.MybatisFactory;
import org.fbi.sbs.repository.dao.ActtscMapper;
import org.fbi.sbs.repository.model.Acttbc;
import org.fbi.sbs.repository.model.Acttmc;
import org.fbi.sbs.repository.model.Acttsc;
import org.fbi.sbs.repository.model.ActtscExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class T8848Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {

        M8848 m8848 = (M8848) m;
        // 获取请求操作的人员ID和日期
        String tellerId = request.getHeader("tellerId");
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

        // 功能码
        int funcde = Integer.parseInt(m8848.getFuncde());
        if (funcde < 0 || funcde > 4) {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            response.setResponseBody(("功能码[" + funcde + "]错误").getBytes(response.getCharacterEncoding()));
            return;
        } else if (funcde == 1) {
            String begnum = m8848.getBegnum();

        } else if (funcde == 0 || funcde == 3) {
            tbc.setTdbsrt(m8848.getPastyp());
            tmc.setTdbsrt(m8848.getPastyp());
            tmc.setTdmsrt(m8848.getInpflg());
            tsc.setTddtdc(m8848.getPastyp() + m8848.getInpflg() + m8848.getSbknum());
        } else if (funcde == 2 || funcde == 4) {
            tbc.setTdbsrt(m8848.getPastyp());
            tmc.setTdbsrt(m8848.getPastyp());
            tmc.setTdmsrt(m8848.getInpflg());
            tsc.setTddtdc(m8848.getPastyp() + m8848.getInpflg() + m8848.getSbknum());
            /*
             MOVE      CTF-PASTYP          TO      TSC-TDBSRT
             MOVE      CTF-INPFLG          TO      TSC-TDMSRT
             MOVE      CTF-SBKNUM          TO      TSC-TDSSRT
             MOVE      TSC-TDDTDC          TO      SQL-TDDTDC

             MOVE      CTF-WRKUNT          TO      TBC-TDBNAM
             MOVE      CTF-STMADD          TO      TMC-TDMNAM
             MOVE      CTF-INTNET          TO      TSC-TDDNAM

             MOVE      CTF-ENGNAM          TO      TSC-TDDCN1
             MOVE      CTF-REGADD          TO      TSC-TDDCN2
             MOVE      CTF-CORADD          TO      TSC-TDDCN3
             MOVE      CTF-CUSNAM          TO      TSC-TDDCN4
             */
        }

        // 查询所有的行业
        List<Acttsc> tcsList = qryAllActtsc();
        // TODO


        response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
        response.setResponseBody("交易完成".getBytes(response.getCharacterEncoding()));
    }

    private List<Acttsc> qryAllActtsc() {
        SqlSession session = MybatisFactory.ORACLE.getInstance().openSession();
        ActtscMapper mapper = session.getMapper(ActtscMapper.class);
        ActtscExample example = new ActtscExample();
        example.setOrderByClause(" TDDTDC desc ");
        return mapper.selectByExample(example);
    }

}
