package com.example.morva.bc.account.infrastructure.user.security.oauth.parse;

import com.example.morva.bc.account.domain.user.type.OauthProvider;
import com.example.morva.bc.account.infrastructure.user.security.oauth.dto.OauthUserInfo;

import java.util.Map;

public interface OauthUserInfoParser {
    OauthUserInfo parse(Map<String, Object> attributes, OauthProvider provider);
}
