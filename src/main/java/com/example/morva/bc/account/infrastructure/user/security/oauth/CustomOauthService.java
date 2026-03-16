package com.example.morva.bc.account.infrastructure.user.security.oauth;

import com.example.morva.bc.account.application.user.internal.OauthUserService;
import com.example.morva.bc.account.domain.user.User;
import com.example.morva.bc.account.domain.user.type.OauthProvider;
import com.example.morva.bc.account.infrastructure.user.security.auth.AuthDetails;
import com.example.morva.bc.account.infrastructure.user.security.oauth.dto.OauthUserInfo;
import com.example.morva.bc.account.infrastructure.user.security.oauth.parse.factory.OauthUserFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOauthService extends DefaultOAuth2UserService {

    private final OauthUserService oauthUserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return process(userRequest, oAuth2User);
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        OauthProvider provider = OauthProvider.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );

        OauthUserInfo oauthUserInfo = OauthUserFactory.getParser(provider, oAuth2User.getAttributes());

        Optional<User> existingUser = oauthUserService.findByEmailAndProvider(
                oauthUserInfo.email(), provider
        );

        if (existingUser.isPresent()) {
            return new AuthDetails(existingUser.get(), oAuth2User.getAttributes());
        }

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(OauthAttribute.ROLE_PENDING.getKey())),
                Map.of(
                        OauthAttribute.EMAIL.getKey(), oauthUserInfo.email(),
                        OauthAttribute.PROVIDER.getKey(), provider.name()
                ),
                OauthAttribute.EMAIL.getKey()
        );
    }
}
