package com.example.movra.bc.account.user.infrastructure.user.security.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OauthAttribute {

    EMAIL("email"),
    PROVIDER("provider"),
    ROLE_PENDING("ROLE_PENDING");

    private final String key;
}
