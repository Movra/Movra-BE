package com.example.movra.bc.insight.behavior_insight.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidAnalysisPeriodException extends CustomException {

    public InvalidAnalysisPeriodException() {
        super(ErrorCode.INVALID_INSIGHT_PERIOD);
    }
}
