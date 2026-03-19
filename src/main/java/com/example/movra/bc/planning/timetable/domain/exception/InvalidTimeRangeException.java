package com.example.movra.bc.planning.timetable.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidTimeRangeException extends CustomException {

    public InvalidTimeRangeException() {
        super(ErrorCode.INVALID_TIME_RANGE);
    }
}
