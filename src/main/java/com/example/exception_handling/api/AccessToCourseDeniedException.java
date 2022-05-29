package com.example.exception_handling.api;

public class AccessToCourseDeniedException extends RuntimeException {
    public AccessToCourseDeniedException(String message) {
        super(message);
    }
}
