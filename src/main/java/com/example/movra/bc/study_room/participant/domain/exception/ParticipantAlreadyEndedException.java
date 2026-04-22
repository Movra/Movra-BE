package com.example.movra.bc.study_room.participant.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class ParticipantAlreadyEndedException extends CustomException {
    public ParticipantAlreadyEndedException() {
        super(ErrorCode.PARTICIPANT_ALREADY_ENDED);
    }
}
