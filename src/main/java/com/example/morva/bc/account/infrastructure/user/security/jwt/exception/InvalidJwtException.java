package com.example.morva.bc.account.infrastructure.user.security.jwt.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class InvalidJwtException extends CustomException {

    public InvalidJwtException() {
        super(ErrorCode.INVALID_JWT);
    }
}
