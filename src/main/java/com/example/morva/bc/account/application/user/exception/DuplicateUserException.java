package com.example.morva.bc.account.application.user.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class DuplicateUserException extends CustomException {

    public DuplicateUserException() {
        super(ErrorCode.DUPLICATE_USER);
    }
}
