package com.example.morva.bc.account.infrastructure.user.security.oauth.parse.naver;

import com.example.morva.bc.account.domain.user.type.OauthProvider;
import com.example.morva.bc.account.infrastructure.user.security.oauth.dto.OauthUserInfo;
import com.example.morva.bc.account.infrastructure.user.security.oauth.parse.OauthUserInfoParser;

import java.util.Map;

public class NaverOAuthUserInfoParser implements OauthUserInfoParser {

    @Override
    public OauthUserInfo parse(Map<String, Object> attributes, OauthProvider provider) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        String email = (String) response.get("email");
        return new OauthUserInfo(email, provider, attributes);
    }
}
