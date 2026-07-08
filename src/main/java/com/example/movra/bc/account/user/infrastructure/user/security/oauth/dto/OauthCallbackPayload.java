package com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto;

import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import lombok.Builder;

import java.util.UUID;

@Builder
public record OauthCallbackPayload(
        OauthCallbackType type,
        UUID userId,
        String email,
        OauthProvider oauthProvider
) {

    public static OauthCallbackPayload existingUser(UUID userId) {
        return OauthCallbackPayload.builder()
                .type(OauthCallbackType.EXISTING_USER)
                .userId(userId)
                .build();
    }

    public static OauthCallbackPayload newUser(String email, OauthProvider oauthProvider) {
        return OauthCallbackPayload.builder()
                .type(OauthCallbackType.NEW_USER)
                .email(email)
                .oauthProvider(oauthProvider)
                .build();
    }
}
