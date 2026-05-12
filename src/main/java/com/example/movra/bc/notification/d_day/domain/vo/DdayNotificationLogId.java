package com.example.movra.bc.notification.d_day.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DdayNotificationLogId(UUID id) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static DdayNotificationLogId newId() {
        return new DdayNotificationLogId(UUID.randomUUID());
    }

    public static DdayNotificationLogId of(UUID id) {
        return new DdayNotificationLogId(id);
    }
}
