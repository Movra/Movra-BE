package com.example.movra.bc.account.infrastructure.user.security.oauth.parse.factory;

import com.example.movra.bc.account.domain.user.type.OauthProvider;
import com.example.movra.bc.account.infrastructure.user.security.oauth.dto.OauthUserInfo;
import com.example.movra.bc.account.infrastructure.user.security.oauth.parse.google.GoogleOAuthUserInfoParser;
import com.example.movra.bc.account.infrastructure.user.security.oauth.parse.naver.NaverOAuthUserInfoParser;

import java.util.Map;

public class OauthUserFactory {

    public static OauthUserInfo getParser(OauthProvider provider, Map<String, Object> attributes){
        return switch (provider){
            case GOOGLE -> new GoogleOAuthUserInfoParser().parse(attributes, provider);
            case NAVER -> new NaverOAuthUserInfoParser().parse(attributes, provider);
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자: " + provider);
        };
    }
}
