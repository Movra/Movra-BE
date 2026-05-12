package com.example.movra.bc.notification.web_push.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebPushSubscriptionKeysRequest(
        @NotBlank
        @Size(max = 512)
        String p256dh,

        @NotBlank
        @Size(max = 256)
        String auth
) {
}
