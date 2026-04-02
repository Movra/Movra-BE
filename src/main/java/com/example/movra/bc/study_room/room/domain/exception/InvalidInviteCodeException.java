package com.example.movra.bc.study_room.room.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidInviteCodeException extends CustomException {
    public InvalidInviteCodeException() {
        super(ErrorCode.INVALID_INVITE_CODE);
    }
}
