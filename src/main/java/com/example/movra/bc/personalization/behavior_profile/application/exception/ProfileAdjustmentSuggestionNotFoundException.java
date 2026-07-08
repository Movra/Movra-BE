package com.example.movra.bc.personalization.behavior_profile.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class ProfileAdjustmentSuggestionNotFoundException extends CustomException {

    public ProfileAdjustmentSuggestionNotFoundException() {
        super(ErrorCode.PROFILE_ADJUSTMENT_SUGGESTION_NOT_FOUND);
    }
}
