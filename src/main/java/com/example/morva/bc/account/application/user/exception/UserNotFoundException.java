package com.example.morva.bc.account.application.user.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class UserNotFoundException extends CustomException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
