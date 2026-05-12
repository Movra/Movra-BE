package com.example.movra.bc.feedback.tiny_win.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidTinyWinException extends CustomException {

    public InvalidTinyWinException() {
        super(ErrorCode.INVALID_TINY_WIN);
    }
}
