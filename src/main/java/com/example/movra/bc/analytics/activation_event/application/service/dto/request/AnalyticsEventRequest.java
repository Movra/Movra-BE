package com.example.movra.bc.analytics.activation_event.application.service.dto.request;

import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record AnalyticsEventRequest(
        @NotNull
        AnalyticsEventType eventType,

        @Size(max = 20)
        Map<String, String> properties
) {
}
