package com.example.morva.bc.account.infrastructure.user.security.oauth.dto;

public record OauthResponse(
        String accessToken,
        String refreshToken,
        boolean isProfileCompleted
) {
}
