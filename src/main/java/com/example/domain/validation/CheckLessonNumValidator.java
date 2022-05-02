package com.example.domain.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckLessonNumValidator implements ConstraintValidator<CheckLessonNum, Number> {
    @Override
    public boolean isValid(Number number, ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }

    @Override
    public void initialize(CheckLessonNum constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
