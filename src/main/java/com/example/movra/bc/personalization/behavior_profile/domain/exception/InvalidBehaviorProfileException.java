package com.example.movra.bc.personalization.behavior_profile.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidBehaviorProfileException extends CustomException {

    public InvalidBehaviorProfileException() {
        super(ErrorCode.INVALID_BEHAVIOR_PROFILE);
    }
}
