package com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto;

import com.example.movra.bc.account.user.domain.user.type.OauthProvider;

import java.util.Map;

public record OauthUserInfo(
        String email,
        OauthProvider provider,
        Map<String, Object> attributes
) {
}
