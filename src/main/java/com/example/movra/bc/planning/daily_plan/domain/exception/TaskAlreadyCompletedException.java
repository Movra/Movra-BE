package com.example.movra.bc.planning.daily_plan.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class TaskAlreadyCompletedException extends CustomException {

    public TaskAlreadyCompletedException() {
        super(ErrorCode.TASK_ALREADY_COMPLETED);
    }
}
