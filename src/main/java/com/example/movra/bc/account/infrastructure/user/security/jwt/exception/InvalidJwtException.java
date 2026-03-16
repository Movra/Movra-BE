package com.example.movra.bc.account.infrastructure.user.security.jwt.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidJwtException extends CustomException {

    public InvalidJwtException() {
        super(ErrorCode.INVALID_JWT);
    }
}
