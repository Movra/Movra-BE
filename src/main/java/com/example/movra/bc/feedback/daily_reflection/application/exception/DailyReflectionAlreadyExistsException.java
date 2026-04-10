package com.example.movra.bc.feedback.daily_reflection.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class DailyReflectionAlreadyExistsException extends CustomException {

    public DailyReflectionAlreadyExistsException() {
        super(ErrorCode.DAILY_REFLECTION_ALREADY_EXISTS);
    }
}
