package com.example.movra.bc.planning.timetable.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class TopPicksNotFullyAssignedException extends CustomException {

    public TopPicksNotFullyAssignedException() {
        super(ErrorCode.TOP_PICKS_NOT_FULLY_ASSIGNED);
    }
}
