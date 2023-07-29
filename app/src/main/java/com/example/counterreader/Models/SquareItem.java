package com.example.counterreader.Models;

import java.util.List;

public class SquareItem {
    public String title;
    public String fileName1;
    public String fileName2;

    public SquareItem(String title, String fileName1, String fileName2) {
        this.title = title;
        this.fileName1 = fileName1;
        this.fileName2 = fileName2;
    }

    public void setFileName1(String fileName1) {
        this.fileName1 = fileName1;
    }

    public void setFileName2(String fileName2) {
        this.fileName2 = fileName2;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSquareTitle() {
        return title;
    }

    public String getFileName1() {
        return fileName1;
    }

    public String getFileName2() {
        return fileName2;
    }
}
