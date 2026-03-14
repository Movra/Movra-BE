package com.example.morva.bc.account.application.user.exception;

import com.example.morva.sharedkernel.exception.CustomException;
import com.example.morva.sharedkernel.exception.ErrorCode;

public class PendingOauthNotFoundException extends CustomException {

    public PendingOauthNotFoundException() {
        super(ErrorCode.PENDING_OAUTH_NOT_FOUND);
    }
}
