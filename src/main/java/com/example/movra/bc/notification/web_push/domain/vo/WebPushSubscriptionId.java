package com.example.movra.bc.notification.web_push.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record WebPushSubscriptionId(UUID id) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static WebPushSubscriptionId newId() {
        return new WebPushSubscriptionId(UUID.randomUUID());
    }

    public static WebPushSubscriptionId of(UUID id) {
        return new WebPushSubscriptionId(id);
    }
}
