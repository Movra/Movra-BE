package com.example.movra.sharedkernel.notification;

import java.util.Map;
import java.util.Objects;

public record NotificationPayload(
        NotificationType type,
        String title,
        String body,
        Map<String, String> data
) {
    public NotificationPayload {
        type = Objects.requireNonNull(type, "type must not be null");
        data = data == null ? Map.of() : Map.copyOf(data);
    }

    public static NotificationPayload of(NotificationType type, String title, String body, Map<String, String> data) {
        return new NotificationPayload(type, title, body, data);
    }
}
