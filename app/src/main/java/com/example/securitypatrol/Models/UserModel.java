package com.example.securitypatrol.Models;

public class UserModel {
    private String username;
    private String uniqueCode;

    public UserModel(String username, String uniqueCode) {
        this.username = username;
        this.uniqueCode = uniqueCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }
}
