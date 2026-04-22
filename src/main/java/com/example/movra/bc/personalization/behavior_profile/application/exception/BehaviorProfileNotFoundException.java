package com.example.movra.bc.personalization.behavior_profile.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class BehaviorProfileNotFoundException extends CustomException {

    public BehaviorProfileNotFoundException() {
        super(ErrorCode.BEHAVIOR_PROFILE_NOT_FOUND);
    }
}
