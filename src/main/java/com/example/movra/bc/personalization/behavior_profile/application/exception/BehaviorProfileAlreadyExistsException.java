package com.example.movra.bc.personalization.behavior_profile.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class BehaviorProfileAlreadyExistsException extends CustomException {

    public BehaviorProfileAlreadyExistsException() {
        super(ErrorCode.BEHAVIOR_PROFILE_ALREADY_EXISTS);
    }
}
