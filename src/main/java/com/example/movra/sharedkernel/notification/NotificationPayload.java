package com.example.movra.sharedkernel.notification;

import java.util.Map;

public record NotificationPayload(
        NotificationType type,
        String title,
        String body,
        Map<String, String> data
) {
    public static NotificationPayload of(NotificationType type, String title, String body, Map<String, String> data) {
        return new NotificationPayload(type, title, body, data);
    }
}
