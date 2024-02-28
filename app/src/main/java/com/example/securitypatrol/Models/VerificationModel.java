package com.example.securitypatrol.Models;

public class VerificationModel {
    private int uniqueId;
    private String tipVerificare;
    private String descriereVerificare;
    private String raspunsVerificare;

    public VerificationModel(int uniqueId, String tipVerificare, String descriereVerificare, String raspunsVerificare) {
        this.uniqueId = uniqueId;
        this.tipVerificare = tipVerificare;
        this.descriereVerificare = descriereVerificare;
        this.raspunsVerificare = raspunsVerificare;
    }

    public VerificationModel(){}

    public int getUniqueId() {
        return uniqueId;
    }

    public String getTipVerificare() {
        return tipVerificare;
    }

    public String getDescriereVerificare() {
        return descriereVerificare;
    }

    public String getRaspunsVerificare() {
        return raspunsVerificare;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setTipVerificare(String tipVerificare) {
        this.tipVerificare = tipVerificare;
    }

    public void setDescriereVerificare(String descriereVerificare) {
        this.descriereVerificare = descriereVerificare;
    }

    public void setRaspunsVerificare(String raspunsVerificare) {
        this.raspunsVerificare = raspunsVerificare;
    }
}
