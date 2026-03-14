package com.example.morva.bc.account.application.user.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class DuplicateAccountIdException extends CustomException {

    public DuplicateAccountIdException() {
        super(ErrorCode.DUPLICATE_ACCOUNT_ID);
    }
}
