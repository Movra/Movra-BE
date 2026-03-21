package com.example.movra.bc.planning.daily_plan.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidTaskTypeException extends CustomException {

    public InvalidTaskTypeException() {
        super(ErrorCode.INVALID_TASK_TYPE);
    }
}
