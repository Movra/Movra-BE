package com.example.movra.bc.planning.daily_plan.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidTopPickEstimatedMinutesException extends CustomException {

    public InvalidTopPickEstimatedMinutesException() {
        super(ErrorCode.INVALID_TOP_PICK_ESTIMATED_MINUTES);
    }
}
