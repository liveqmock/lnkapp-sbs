package org.fbi.sbs.repository.model;

import java.util.Date;

public class Acttbc {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SBS.ACTTBC.TDBSRT
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    private String tdbsrt;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SBS.ACTTBC.TDBNAM
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    private String tdbnam;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SBS.ACTTBC.AMDTLR
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    private String amdtlr;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SBS.ACTTBC.UPDDAT
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    private Date upddat;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SBS.ACTTBC.TDBSRT
     *
     * @return the value of SBS.ACTTBC.TDBSRT
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    public String getTdbsrt() {
        return tdbsrt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SBS.ACTTBC.TDBSRT
     *
     * @param tdbsrt the value for SBS.ACTTBC.TDBSRT
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    public void setTdbsrt(String tdbsrt) {
        this.tdbsrt = tdbsrt == null ? null : tdbsrt.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SBS.ACTTBC.TDBNAM
     *
     * @return the value of SBS.ACTTBC.TDBNAM
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    public String getTdbnam() {
        return tdbnam;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SBS.ACTTBC.TDBNAM
     *
     * @param tdbnam the value for SBS.ACTTBC.TDBNAM
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    public void setTdbnam(String tdbnam) {
        this.tdbnam = tdbnam == null ? null : tdbnam.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SBS.ACTTBC.AMDTLR
     *
     * @return the value of SBS.ACTTBC.AMDTLR
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    public String getAmdtlr() {
        return amdtlr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SBS.ACTTBC.AMDTLR
     *
     * @param amdtlr the value for SBS.ACTTBC.AMDTLR
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    public void setAmdtlr(String amdtlr) {
        this.amdtlr = amdtlr == null ? null : amdtlr.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SBS.ACTTBC.UPDDAT
     *
     * @return the value of SBS.ACTTBC.UPDDAT
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    public Date getUpddat() {
        return upddat;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SBS.ACTTBC.UPDDAT
     *
     * @param upddat the value for SBS.ACTTBC.UPDDAT
     *
     * @mbggenerated Thu Apr 10 13:36:10 CST 2014
     */
    public void setUpddat(Date upddat) {
        this.upddat = upddat;
    }
}