package com.example.movra.bc.study_room.participant.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class ParticipantNotFoundException extends CustomException {
    public ParticipantNotFoundException() {
        super(ErrorCode.PARTICIPANT_NOT_FOUND);
    }
}
