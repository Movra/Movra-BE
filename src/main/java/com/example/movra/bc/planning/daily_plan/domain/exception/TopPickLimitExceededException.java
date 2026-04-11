package com.example.movra.bc.planning.daily_plan.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class TopPickLimitExceededException extends CustomException {

    public TopPickLimitExceededException() {
        super(ErrorCode.CORE_SELECTED_LIMIT_EXCEEDED);
    }
}
