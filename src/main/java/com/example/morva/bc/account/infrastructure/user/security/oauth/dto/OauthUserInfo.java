package com.example.morva.bc.account.infrastructure.user.security.oauth.dto;

import com.example.morva.bc.account.domain.user.type.OauthProvider;

import java.util.Map;

public record OauthUserInfo(
        String email,
        OauthProvider provider,
        Map<String, Object> attributes
) {
}
