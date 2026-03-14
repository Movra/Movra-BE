package com.example.morva.bc.account.application.user.dto.response;

import lombok.Builder;

@Builder
public record ProfileSetupResponse(
        String accessToken,
        String refreshToken,
        boolean isProfileCompleted
) {
}
