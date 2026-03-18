package com.example.movra.bc.planning.timetable.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class SlotNotFoundException extends CustomException {

    public SlotNotFoundException() {
        super(ErrorCode.SLOT_NOT_FOUND);
    }
}
