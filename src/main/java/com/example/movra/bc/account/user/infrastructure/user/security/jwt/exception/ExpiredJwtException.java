package com.example.movra.bc.account.user.infrastructure.user.security.jwt.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class ExpiredJwtException extends CustomException {

    public ExpiredJwtException() {
        super(ErrorCode.EXPIRED_JWT);
    }
}
