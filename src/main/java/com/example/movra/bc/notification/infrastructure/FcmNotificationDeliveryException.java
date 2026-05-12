package com.example.movra.bc.notification.infrastructure;

public class FcmNotificationDeliveryException extends RuntimeException {

    public FcmNotificationDeliveryException(String message) {
        super(message);
    }

    public FcmNotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
