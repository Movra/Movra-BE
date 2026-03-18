package com.example.movra.bc.planning.timetable.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class TimetableNotFoundException extends CustomException {

    public TimetableNotFoundException() {
        super(ErrorCode.TIMETABLE_NOT_FOUND);
    }
}
