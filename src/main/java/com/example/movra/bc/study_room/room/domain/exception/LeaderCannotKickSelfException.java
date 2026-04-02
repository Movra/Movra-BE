package com.example.movra.bc.study_room.room.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class LeaderCannotKickSelfException extends CustomException {
    public LeaderCannotKickSelfException() {
        super(ErrorCode.LEADER_CANNOT_KICK_SELF);
    }
}
