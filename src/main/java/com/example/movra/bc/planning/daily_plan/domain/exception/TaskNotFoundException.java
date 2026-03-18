package com.example.movra.bc.planning.daily_plan.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class TaskNotFoundException extends CustomException {

    public TaskNotFoundException() {
        super(ErrorCode.TASK_NOT_FOUND);
    }
}
