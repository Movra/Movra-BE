package com.example.movra.bc.feedback.daily_reflection.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidDailyReflectionException extends CustomException {

    public InvalidDailyReflectionException() {
        super(ErrorCode.INVALID_DAILY_REFLECTION);
    }
}
