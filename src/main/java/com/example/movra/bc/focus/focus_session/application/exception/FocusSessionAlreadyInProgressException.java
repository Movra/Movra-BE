package com.example.movra.bc.focus.focus_session.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class FocusSessionAlreadyInProgressException extends CustomException {

    public FocusSessionAlreadyInProgressException() {
        super(ErrorCode.FOCUS_SESSION_ALREADY_IN_PROGRESS);
    }
}
