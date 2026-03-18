package com.example.movra.bc.planning.daily_plan.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class DailyPlanAlreadyExistsException extends CustomException {

    public DailyPlanAlreadyExistsException() {
        super(ErrorCode.DAILY_PLAN_ALREADY_EXISTS);
    }
}
