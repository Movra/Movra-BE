package com.example.movra.bc.notification.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record NotificationPreferenceId(UUID id) {

    public static NotificationPreferenceId newId() {
        return new NotificationPreferenceId(UUID.randomUUID());
    }

    public static NotificationPreferenceId of(UUID id) {
        return new NotificationPreferenceId(id);
    }
}
