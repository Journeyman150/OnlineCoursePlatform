package com.example.exception_handling.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice("com.example.controllers.api")
public class ApiExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<IncorrectCourseData> handleException(NoSuchCourseException exception) {
        IncorrectCourseData data = new IncorrectCourseData(exception.getMessage());
        return new ResponseEntity<>(data, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<IncorrectCourseData> handleException(AccessToCourseDeniedException exception) {
        IncorrectCourseData data = new IncorrectCourseData(exception.getMessage());
        return new ResponseEntity<>(data, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<IncorrectCourseData> handleException(Exception exception) {
        IncorrectCourseData data = new IncorrectCourseData(exception.getMessage());
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
    }


}
