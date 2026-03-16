package com.example.movra.bc.account.infrastructure.user.security.oauth.parse;

import com.example.movra.bc.account.domain.user.type.OauthProvider;
import com.example.movra.bc.account.infrastructure.user.security.oauth.dto.OauthUserInfo;

import java.util.Map;

public interface OauthUserInfoParser {
    OauthUserInfo parse(Map<String, Object> attributes, OauthProvider provider);
}
