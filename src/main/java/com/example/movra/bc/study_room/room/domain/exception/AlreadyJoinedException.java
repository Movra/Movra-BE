package com.example.movra.bc.study_room.room.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class AlreadyJoinedException extends CustomException {
    public AlreadyJoinedException() {
        super(ErrorCode.ALREADY_JOINED);
    }
}
