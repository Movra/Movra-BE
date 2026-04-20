package com.example.movra.bc.accountability.accountability_relation.application.service.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidDateRangeException extends CustomException {

    public InvalidDateRangeException() {
        super(ErrorCode.INVALID_DATE_RANGE);
    }
}
