package com.example.movra.bc.account.infrastructure.user.security.oauth.dto;

import com.example.movra.bc.account.domain.user.type.OauthProvider;
import lombok.Builder;

@Builder
public record PendingOauth(
        String email,
        OauthProvider oauthProvider
) {}
