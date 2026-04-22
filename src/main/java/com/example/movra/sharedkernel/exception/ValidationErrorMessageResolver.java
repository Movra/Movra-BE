package com.example.movra.sharedkernel.exception;

import org.springframework.validation.BindingResult;

public final class ValidationErrorMessageResolver {

    private ValidationErrorMessageResolver() {
    }

    public static String resolve(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());
    }
}
