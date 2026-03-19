package com.example.movra.bc.planning.daily_plan.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class DailyPlanNotFoundException extends CustomException {

    public DailyPlanNotFoundException() {
        super(ErrorCode.DAILY_PLAN_NOT_FOUND);
    }
}
