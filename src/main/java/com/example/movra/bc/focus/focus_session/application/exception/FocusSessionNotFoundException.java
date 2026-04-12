package com.example.movra.bc.focus.focus_session.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class FocusSessionNotFoundException extends CustomException {

    public FocusSessionNotFoundException() {
        super(ErrorCode.FOCUS_SESSION_NOT_FOUND);
    }
}
