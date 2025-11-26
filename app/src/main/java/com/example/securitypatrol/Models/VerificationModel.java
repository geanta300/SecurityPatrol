package com.example.securitypatrol.Models;

public class VerificationModel {
    private int uniqueId;
    private int tipVerificare;
    private String descriereVerificare;
    private String valoriVerificare;
    private String raspunsVerificare;

    public VerificationModel(int uniqueId, int tipVerificare, String descriereVerificare, String valoriVerificare, String raspunsVerificare) {
        this.uniqueId = uniqueId;
        this.tipVerificare = tipVerificare;
        this.descriereVerificare = descriereVerificare;
        this.valoriVerificare = valoriVerificare;
        this.raspunsVerificare = raspunsVerificare;
    }

    public VerificationModel(){}

    public int getUniqueId() {
        return uniqueId;
    }

    public int getTipVerificare() {
        return tipVerificare;
    }

    public String getDescriereVerificare() {
        return descriereVerificare;
    }

    public String getValoriVerificare() {
        return valoriVerificare;
    }

    public String getRaspunsVerificare() {
        return raspunsVerificare;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setTipVerificare(int tipVerificare) {
        this.tipVerificare = tipVerificare;
    }

    public void setDescriereVerificare(String descriereVerificare) {
        this.descriereVerificare = descriereVerificare;
    }

    public void setValoriVerificare(String valoriVerificare) {
        this.valoriVerificare = valoriVerificare;
    }

    public void setRaspunsVerificare(String raspunsVerificare) {
        this.raspunsVerificare = raspunsVerificare;
    }
}
