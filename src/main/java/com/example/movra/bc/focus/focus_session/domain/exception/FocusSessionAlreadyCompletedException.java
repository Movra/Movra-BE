package com.example.movra.bc.focus.focus_session.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class FocusSessionAlreadyCompletedException extends CustomException {

    public FocusSessionAlreadyCompletedException() {
        super(ErrorCode.FOCUS_SESSION_ALREADY_COMPLETED);
    }
}
