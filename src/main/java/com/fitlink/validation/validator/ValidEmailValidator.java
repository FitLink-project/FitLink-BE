package com.fitlink.validation.validator;

import com.fitlink.validation.annotation.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // null 또는 빈 문자열은 @NotBlank에서 처리
        if (email == null || email.isBlank()) {
            return true;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }
}

