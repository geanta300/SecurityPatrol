package com.example.securitypatrol.Models;

public class SquareItem {
    public String title;
    public String fileName1;

    public SquareItem(String title, String fileName1) {
        this.title = title;
        this.fileName1 = fileName1;
    }

    public String getSquareTitle() {
        return title;
    }

    public String getFileName1() {
        return fileName1;
    }

}
