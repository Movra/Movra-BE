package com.example.movra.bc.account.user.infrastructure.user.security.oauth.parse.google;

import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.OauthUserInfo;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.parse.OauthUserInfoParser;

import java.util.Map;

public class GoogleOAuthUserInfoParser implements OauthUserInfoParser {

    @Override
    public OauthUserInfo parse(Map<String, Object> attributes, OauthProvider provider) {
        String email = (String) attributes.get("email");
        return new OauthUserInfo(email, provider, attributes);
    }
}
