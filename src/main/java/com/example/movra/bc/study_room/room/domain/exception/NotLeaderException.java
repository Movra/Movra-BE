package com.example.movra.bc.study_room.room.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class NotLeaderException extends CustomException {
    public NotLeaderException() {
        super(ErrorCode.NOT_LEADER);
    }
}
