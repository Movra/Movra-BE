package com.example.movra.bc.analytics.activation_event.application.service.dto.response;

import com.example.movra.bc.analytics.activation_event.domain.AnalyticsEvent;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Builder
public record AnalyticsEventResponse(
        UUID analyticsEventId,
        AnalyticsEventType eventType,
        Instant occurredAt,
        Map<String, String> properties
) {

    public static AnalyticsEventResponse from(AnalyticsEvent analyticsEvent) {
        return AnalyticsEventResponse.builder()
                .analyticsEventId(analyticsEvent.getId().id())
                .eventType(analyticsEvent.getEventType())
                .occurredAt(analyticsEvent.getOccurredAt())
                .properties(analyticsEvent.getProperties())
                .build();
    }
}
