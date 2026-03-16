package com.example.movra.bc.account.application.user.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class PendingOauthNotFoundException extends CustomException {

    public PendingOauthNotFoundException() {
        super(ErrorCode.PENDING_OAUTH_NOT_FOUND);
    }
}
