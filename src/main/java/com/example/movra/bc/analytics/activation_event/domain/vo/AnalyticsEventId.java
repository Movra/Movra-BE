package com.example.movra.bc.analytics.activation_event.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record AnalyticsEventId(UUID id) {

    public static AnalyticsEventId newId() {
        return new AnalyticsEventId(UUID.randomUUID());
    }

    public static AnalyticsEventId of(UUID id) {
        return new AnalyticsEventId(id);
    }
}
