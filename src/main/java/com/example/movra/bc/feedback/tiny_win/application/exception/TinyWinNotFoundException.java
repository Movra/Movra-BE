package com.example.movra.bc.feedback.tiny_win.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class TinyWinNotFoundException extends CustomException {

    public TinyWinNotFoundException() {
        super(ErrorCode.TINY_WIN_NOT_FOUND);
    }
}
