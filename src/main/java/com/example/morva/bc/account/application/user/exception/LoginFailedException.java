package com.example.morva.bc.account.application.user.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class LoginFailedException extends CustomException {

    public LoginFailedException() {
        super(ErrorCode.LOGIN_FAILED);
    }
}
