package com.example.morva.sharedkernel.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class NotEmptyMultipartFileValidator implements ConstraintValidator<NotEmptyMultipartFile, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        if (file == null) return false;
        return !file.isEmpty();
    }
}
