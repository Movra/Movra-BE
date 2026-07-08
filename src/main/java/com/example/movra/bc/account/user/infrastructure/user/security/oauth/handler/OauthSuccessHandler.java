package com.example.movra.bc.account.user.infrastructure.user.security.oauth.handler;

import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.auth.AuthDetails;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.OauthAttribute;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.OauthCallbackPayload;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending.OauthCallbackStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OauthCallbackStore oauthCallbackStore;

    @Value("${spring.oauth.redirect.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthDetails authDetails) {
            handleExistingUser(response, authDetails);
        } else if (principal instanceof OAuth2User oAuth2User) {
            handleNewUser(response, oAuth2User);
        }
    }

    private void handleExistingUser(HttpServletResponse response, AuthDetails authDetails) throws IOException {
        String code = oauthCallbackStore.save(
                OauthCallbackPayload.existingUser(authDetails.getUser().getId().id())
        );

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("code", code)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private void handleNewUser(HttpServletResponse response, OAuth2User oAuth2User) throws IOException {
        String email = oAuth2User.getAttribute(OauthAttribute.EMAIL.getKey());
        OauthProvider provider = OauthProvider.valueOf(oAuth2User.getAttribute(OauthAttribute.PROVIDER.getKey()));

        String code = oauthCallbackStore.save(
                OauthCallbackPayload.newUser(email, provider)
        );

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("code", code)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
