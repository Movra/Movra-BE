package com.example.movra.bc.study_room.participant.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class NotFocusingException extends CustomException {
    public NotFocusingException() {
        super(ErrorCode.NOT_FOCUSING);
    }
}
