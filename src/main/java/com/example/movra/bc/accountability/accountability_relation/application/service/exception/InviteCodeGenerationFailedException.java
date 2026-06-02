package com.example.movra.bc.accountability.accountability_relation.application.service.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InviteCodeGenerationFailedException extends CustomException {

    public InviteCodeGenerationFailedException() {
        super(ErrorCode.INVITE_CODE_GENERATION_FAILED);
    }
}
