package com.example.exception_handling.api;

public class NoSuchCourseException extends RuntimeException {
    public NoSuchCourseException(String message) {
        super(message);
    }
}
