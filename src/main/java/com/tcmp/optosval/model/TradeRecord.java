package com.tcmp.optosval.model;

import java.util.Date;

public class TradeRecord {
    private String BRANCH;
    private String PRODUCTO;
    private String DEAL;
    private String FECHA_LIQ;
    private String DESCR;
    private String ID_CONTRAPARTE;
    private String NOMBRE;
    private String CCOSTOS;
    private String MONEDA_NOCIONAL;
    private double NOCIONAL;
    private double TIPO_CAMBIO;
    private String CONTRAMONEDA;
    private double CONTRAMONTO;
    private String MONEDA_LIQ;
    private double MONTO_LIQUIDAR;
    private String SUBYACENTE;

    // Constructor vacío
    public TradeRecord() {
    }

    // Getters y setters
    public String getBRANCH() {
        return BRANCH;
    }

    public void setBRANCH(String BRANCH) {
        this.BRANCH = BRANCH;
    }

    public String getPRODUCTO() {
        return PRODUCTO;
    }

    public void setPRODUCTO(String PRODUCTO) {
        this.PRODUCTO = PRODUCTO;
    }

    public String getDEAL() {
        return DEAL;
    }

    public void setDEAL(String DEAL) {
        this.DEAL = DEAL;
    }

    public String getFECHA_LIQ() {
        return FECHA_LIQ;
    }

    public void setFECHA_LIQ(String FECHA_LIQ) {
        this.FECHA_LIQ = FECHA_LIQ;
    }

    public String getDESCR() {
        return DESCR;
    }

    public void setDESCR(String DESCR) {
        this.DESCR = DESCR;
    }

    public String getID_CONTRAPARTE() {
        return ID_CONTRAPARTE;
    }

    public void setID_CONTRAPARTE(String ID_CONTRAPARTE) {
        this.ID_CONTRAPARTE = ID_CONTRAPARTE;
    }

    public String getNOMBRE() {
        return NOMBRE;
    }

    public void setNOMBRE(String NOMBRE) {
        this.NOMBRE = NOMBRE;
    }

    public String getCCOSTOS() {
        return CCOSTOS;
    }

    public void setCCOSTOS(String CCOSTOS) {
        this.CCOSTOS = CCOSTOS;
    }

    public String getMONEDA_NOCIONAL() {
        return MONEDA_NOCIONAL;
    }

    public void setMONEDA_NOCIONAL(String MONEDA_NOCIONAL) {
        this.MONEDA_NOCIONAL = MONEDA_NOCIONAL;
    }

    public double getNOCIONAL() {
        return NOCIONAL;
    }

    public void setNOCIONAL(double NOCIONAL) {
        this.NOCIONAL = NOCIONAL;
    }

    public double getTIPO_CAMBIO() {
        return TIPO_CAMBIO;
    }

    public void setTIPO_CAMBIO(double TIPO_CAMBIO) {
        this.TIPO_CAMBIO = TIPO_CAMBIO;
    }

    public String getCONTRAMONEDA() {
        return CONTRAMONEDA;
    }

    public void setCONTRAMONEDA(String CONTRAMONEDA) {
        this.CONTRAMONEDA = CONTRAMONEDA;
    }

    public double getCONTRAMONTO() {
        return CONTRAMONTO;
    }

    public void setCONTRAMONTO(double CONTRAMONTO) {
        this.CONTRAMONTO = CONTRAMONTO;
    }

    public String getMONEDA_LIQ() {
        return MONEDA_LIQ;
    }

    public void setMONEDA_LIQ(String MONEDA_LIQ) {
        this.MONEDA_LIQ = MONEDA_LIQ;
    }

    public double getMONTO_LIQUIDAR() {
        return MONTO_LIQUIDAR;
    }

    public void setMONTO_LIQUIDAR(double MONTO_LIQUIDAR) {
        this.MONTO_LIQUIDAR = MONTO_LIQUIDAR;
    }

    public String getSUBYACENTE() {
        return SUBYACENTE;
    }

    public void setSUBYACENTE(String SUBYACENTE) {
        this.SUBYACENTE = SUBYACENTE;
    }
}