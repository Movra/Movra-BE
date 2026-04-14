package com.example.movra.bc.account.device_token.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UnregisterDeviceTokenRequest(
        @NotBlank String token
) {
}
