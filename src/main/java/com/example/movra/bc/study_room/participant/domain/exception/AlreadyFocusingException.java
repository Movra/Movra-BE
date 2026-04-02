package com.example.movra.bc.study_room.participant.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class AlreadyFocusingException extends CustomException {
    public AlreadyFocusingException() {
        super(ErrorCode.ALREADY_FOCUSING);
    }
}
