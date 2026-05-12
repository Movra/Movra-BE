package com.example.movra.bc.notification.web_push.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidWebPushSubscriptionException extends CustomException {

    public InvalidWebPushSubscriptionException() {
        super(ErrorCode.INVALID_WEB_PUSH_SUBSCRIPTION);
    }
}
