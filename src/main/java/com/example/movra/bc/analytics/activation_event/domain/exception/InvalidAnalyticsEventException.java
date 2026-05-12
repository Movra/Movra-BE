package com.example.movra.bc.analytics.activation_event.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidAnalyticsEventException extends CustomException {

    public InvalidAnalyticsEventException() {
        super(ErrorCode.INVALID_ANALYTICS_EVENT);
    }
}
