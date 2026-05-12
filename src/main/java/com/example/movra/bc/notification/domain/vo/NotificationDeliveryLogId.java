package com.example.movra.bc.notification.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record NotificationDeliveryLogId(UUID id) {

    public static NotificationDeliveryLogId newId() {
        return new NotificationDeliveryLogId(UUID.randomUUID());
    }

    public static NotificationDeliveryLogId of(UUID id) {
        return new NotificationDeliveryLogId(id);
    }
}
