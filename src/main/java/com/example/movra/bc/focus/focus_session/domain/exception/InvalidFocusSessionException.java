package com.example.movra.bc.focus.focus_session.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidFocusSessionException extends CustomException {

    public InvalidFocusSessionException() {
        super(ErrorCode.INVALID_FOCUS_SESSION);
    }
}
