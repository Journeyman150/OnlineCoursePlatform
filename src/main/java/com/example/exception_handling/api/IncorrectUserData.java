package com.example.exception_handling.api;

public class IncorrectUserData {
    private String info;

    public IncorrectUserData(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
