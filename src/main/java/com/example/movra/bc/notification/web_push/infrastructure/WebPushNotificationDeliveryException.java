package com.example.movra.bc.notification.web_push.infrastructure;

public class WebPushNotificationDeliveryException extends RuntimeException {

    public WebPushNotificationDeliveryException(String message) {
        super(message);
    }

    public WebPushNotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
