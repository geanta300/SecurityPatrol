package com.example.securitypatrol.Models;

public class ScanatModel {
    private int id;
    private String dataTime;
    private int objectiveID;

    public ScanatModel() {}

    public int getId() {
        return id;
    }

    public String getDataTime() {
        return dataTime;
    }

    public int getObjectiveID() {
        return objectiveID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public void setObjectiveID(int objectiveID) {
        this.objectiveID = objectiveID;
    }
}
