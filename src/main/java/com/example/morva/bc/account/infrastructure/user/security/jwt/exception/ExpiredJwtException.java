package com.example.morva.bc.account.infrastructure.user.security.jwt.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class ExpiredJwtException extends CustomException {

    public ExpiredJwtException() {
        super(ErrorCode.EXPIRED_JWT);
    }
}
