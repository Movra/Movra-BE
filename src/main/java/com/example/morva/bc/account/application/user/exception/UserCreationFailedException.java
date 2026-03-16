package com.example.morva.bc.account.application.user.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class UserCreationFailedException extends CustomException {

    public UserCreationFailedException() {
        super(ErrorCode.USER_CREATION_FAILED);
    }
}
