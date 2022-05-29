package com.example.exception_handling.api;

public class AccessToLessonDeniedException extends RuntimeException {
    public AccessToLessonDeniedException(String message) {
        super(message);
    }
}
