package com.example.movra.bc.account.application.user.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class UserCreationFailedException extends CustomException {

    public UserCreationFailedException() {
        super(ErrorCode.USER_CREATION_FAILED);
    }
}
