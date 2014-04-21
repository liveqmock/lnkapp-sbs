package org.fbi.sbs.repository.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.fbi.sbs.repository.model.Acttsc;
import org.fbi.sbs.repository.model.ActtscExample;

public interface ActtscMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SBS.ACTTSC
     *
     * @mbggenerated Thu Apr 10 10:56:27 CST 2014
     */
    int countByExample(ActtscExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SBS.ACTTSC
     *
     * @mbggenerated Thu Apr 10 10:56:27 CST 2014
     */
    int deleteByExample(ActtscExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SBS.ACTTSC
     *
     * @mbggenerated Thu Apr 10 10:56:27 CST 2014
     */
    int insert(Acttsc record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SBS.ACTTSC
     *
     * @mbggenerated Thu Apr 10 10:56:27 CST 2014
     */
    int insertSelective(Acttsc record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SBS.ACTTSC
     *
     * @mbggenerated Thu Apr 10 10:56:27 CST 2014
     */
    List<Acttsc> selectByExample(ActtscExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SBS.ACTTSC
     *
     * @mbggenerated Thu Apr 10 10:56:27 CST 2014
     */
    int updateByExampleSelective(@Param("record") Acttsc record, @Param("example") ActtscExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SBS.ACTTSC
     *
     * @mbggenerated Thu Apr 10 10:56:27 CST 2014
     */
    int updateByExample(@Param("record") Acttsc record, @Param("example") ActtscExample example);

    @Select(" SELECT COUNT(*) FROM ACTTSC" +
            " WHERE SUBSTR(TDDTDC,1,1) = #{tdbsrt}" +
            " AND SUBSTR(TDDTDC,2,2) = #{tdmsrt}")
    int countSameTmc(@Param("tdbsrt") String tdbsrt, @Param("tdmsrt") String tdmsrt);

    @Select(" select tddtdc,tddnam,tddcn1,tddcn2,tddcn3,tddcn4,amdtlr,upddat " +
            " from" +
            "    (" +
            "     select tsc.*,rownum rn" +
            "     from (" +
            "           select * from acttsc order by tddtdc desc" +
            "          ) tsc" +
            "     ) t" +
            " where t.rn between #{start} and #{end}")
    List<Acttsc> qryByIndex(@Param("start") String start, @Param("end") String end);


}