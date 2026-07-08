package com.example.movra.bc.account.user.application.user.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class OauthCallbackNotFoundException extends CustomException {

    public OauthCallbackNotFoundException() {
        super(ErrorCode.OAUTH_CALLBACK_NOT_FOUND);
    }
}
