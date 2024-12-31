package com.tcmp.optosval.model;

public class OptoRecord {
    private String INST;
    private String CONT;
    private String FECHA;
    private String NU_ID;
    private String NU_PE_EJE;
    private Double IMPBA_CO;
    private String FEINOP_CO;
    private String FEVEOP_CO;
    private String SUBY_CO;
    private String CVE_TIT_C;
    private Double PRECIOEJER_C;
    private Double PRE_SUP;
    private Double PRE_INF;
    private String INST_LEI;
    private String UTI;
    private String IDENTIFICADOR;

    // Constructor vacío
    public OptoRecord() {
    }

    // Getters y setters
    public String getCONT() {
        return CONT;
    }

    public void setCONT(String CONT) {
        this.CONT = CONT;
    }

    public String getINST() {
        return INST;
    }

    public void setINST(String INST) {
        this.INST = INST;
    }

    public String getFECHA() {
        return FECHA;
    }

    public void setFECHA(String FECHA) {
        this.FECHA = FECHA;
    }

    public String getNU_ID() {
        return NU_ID;
    }

    public void setNU_ID(String NU_ID) {
        this.NU_ID = NU_ID;
    }

    public String getNU_PE_EJE() {
        return NU_PE_EJE;
    }

    public void setNU_PE_EJE(String NU_PE_EJE) {
        this.NU_PE_EJE = NU_PE_EJE;
    }

    public Double getIMPBA_CO() {
        return IMPBA_CO;
    }

    public void setIMPBA_CO(Double IMPBA_CO) {
        this.IMPBA_CO = IMPBA_CO;
    }

    public String getFEINOP_CO() {
        return FEINOP_CO;
    }

    public void setFEINOP_CO(String FEINOP_CO) {
        this.FEINOP_CO = FEINOP_CO;
    }

    public String getFEVEOP_CO() {
        return FEVEOP_CO;
    }

    public void setFEVEOP_CO(String FEVEOP_CO) {
        this.FEVEOP_CO = FEVEOP_CO;
    }

    public String getSUBY_CO() {
        return SUBY_CO;
    }

    public void setSUBY_CO(String SUBY_CO) {
        this.SUBY_CO = SUBY_CO;
    }

    public String getCVE_TIT_C() {
        return CVE_TIT_C;
    }

    public void setCVE_TIT_C(String CVE_TIT_C) {
        this.CVE_TIT_C = CVE_TIT_C;
    }

    public Double getPRECIOEJER_C() {
        return PRECIOEJER_C;
    }

    public void setPRECIOEJER_C(Double PRECIOEJER_C) {
        this.PRECIOEJER_C = PRECIOEJER_C;
    }

    public Double getPRE_SUP() {
        return PRE_SUP;
    }

    public void setPRE_SUP(Double PRE_SUP) {
        this.PRE_SUP = PRE_SUP;
    }

    public Double getPRE_INF() {
        return PRE_INF;
    }

    public void setPRE_INF(Double PRE_INF) {
        this.PRE_INF = PRE_INF;
    }

    public String getINST_LEI() {
        return INST_LEI;
    }

    public void setINST_LEI(String INST_LEI) {
        this.INST_LEI = INST_LEI;
    }

    public String getUTI() {
        return UTI;
    }

    public void setUTI(String UTI) {
        this.UTI = UTI;
    }

    public String getIDENTIFICADOR() {
        return IDENTIFICADOR;
    }

    public void setIDENTIFICADOR(String IDENTIFICADOR) {
        this.IDENTIFICADOR = IDENTIFICADOR;
    }
}