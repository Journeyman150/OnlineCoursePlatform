package com.example.exception_handling.api;

public class InvalidUserDataException extends RuntimeException {
    public InvalidUserDataException(String message) {
        super(message);
    }
}
