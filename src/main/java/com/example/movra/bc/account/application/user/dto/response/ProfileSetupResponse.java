package com.example.movra.bc.account.application.user.dto.response;

import lombok.Builder;

@Builder
public record ProfileSetupResponse(
        String accessToken,
        String refreshToken,
        boolean isProfileCompleted
) {
}
