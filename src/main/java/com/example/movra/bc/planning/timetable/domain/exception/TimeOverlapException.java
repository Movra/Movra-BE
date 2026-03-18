package com.example.movra.bc.planning.timetable.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class TimeOverlapException extends CustomException {

    public TimeOverlapException() {
        super(ErrorCode.TIME_OVERLAP);
    }
}
