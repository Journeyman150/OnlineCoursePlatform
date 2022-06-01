package com.example.exception_handling;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleException(MaxUploadSizeExceededException exception) {
        ModelAndView modelAndView = new ModelAndView("error/file_size_error");
        modelAndView.getModel().put("errorMessage", "Upload file size exceeds limit!");
        return modelAndView;
    }
}
