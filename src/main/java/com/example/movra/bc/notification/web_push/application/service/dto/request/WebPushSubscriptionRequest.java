package com.example.movra.bc.notification.web_push.application.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WebPushSubscriptionRequest(
        @NotBlank
        @Size(max = 2048)
        String endpoint,

        @Valid
        @NotNull
        WebPushSubscriptionKeysRequest keys,

        @Size(max = 32)
        String contentEncoding,

        @Size(max = 512)
        String userAgent
) {
}
