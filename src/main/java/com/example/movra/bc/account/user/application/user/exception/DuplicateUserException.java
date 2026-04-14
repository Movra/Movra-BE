package com.example.movra.bc.account.user.application.user.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class DuplicateUserException extends CustomException {

    public DuplicateUserException() {
        super(ErrorCode.DUPLICATE_USER);
    }
}
