package com.example.exception_handling.api;

public class IncorrectCourseData {
    private String info;

    public IncorrectCourseData(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
