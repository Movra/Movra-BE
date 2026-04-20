package com.example.movra.bc.accountability.accountability_relation.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InviteCodeExpiredException extends CustomException {

    public InviteCodeExpiredException() {
        super(ErrorCode.INVITE_CODE_EXPIRED);
    }
}
