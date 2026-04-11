package com.example.movra.bc.feedback.daily_reflection.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class DailyReflectionNotFoundException extends CustomException {

    public DailyReflectionNotFoundException() {
        super(ErrorCode.DAILY_REFLECTION_NOT_FOUND);
    }
}
