package com.example.movra.bc.notification.web_push.application.service.dto.response;

import com.example.movra.bc.notification.web_push.domain.WebPushSubscription;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record WebPushSubscriptionResponse(
        UUID webPushSubscriptionId,
        String endpoint,
        String contentEncoding,
        Instant createdAt,
        Instant lastRegisteredAt
) {

    public static WebPushSubscriptionResponse from(WebPushSubscription subscription) {
        return WebPushSubscriptionResponse.builder()
                .webPushSubscriptionId(subscription.getId().id())
                .endpoint(subscription.getEndpoint())
                .contentEncoding(subscription.getContentEncoding())
                .createdAt(subscription.getCreatedAt())
                .lastRegisteredAt(subscription.getLastRegisteredAt())
                .build();
    }
}
