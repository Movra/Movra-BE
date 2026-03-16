package com.example.movra.bc.account.application.user.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class DuplicateAccountIdException extends CustomException {

    public DuplicateAccountIdException() {
        super(ErrorCode.DUPLICATE_ACCOUNT_ID);
    }
}
