package com.example.movra.bc.study_room.room.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class PrivateRoomRequiresInviteCodeException extends CustomException {
    public PrivateRoomRequiresInviteCodeException() {
        super(ErrorCode.PRIVATE_ROOM_REQUIRES_INVITE_CODE);
    }
}
