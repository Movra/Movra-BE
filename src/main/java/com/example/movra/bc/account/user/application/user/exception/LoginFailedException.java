package com.example.movra.bc.account.user.application.user.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class LoginFailedException extends CustomException {

    public LoginFailedException() {
        super(ErrorCode.LOGIN_FAILED);
    }
}
