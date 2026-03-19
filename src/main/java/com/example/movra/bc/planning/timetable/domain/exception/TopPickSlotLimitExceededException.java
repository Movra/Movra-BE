package com.example.movra.bc.planning.timetable.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class TopPickSlotLimitExceededException extends CustomException {

    public TopPickSlotLimitExceededException() {
        super(ErrorCode.TOP_PICK_SLOT_LIMIT_EXCEEDED);
    }
}
