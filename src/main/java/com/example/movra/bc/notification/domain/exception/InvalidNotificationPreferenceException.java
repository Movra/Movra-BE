package com.example.movra.bc.notification.domain.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidNotificationPreferenceException extends CustomException {

    public InvalidNotificationPreferenceException() {
        super(ErrorCode.INVALID_NOTIFICATION_PREFERENCE);
    }
}
