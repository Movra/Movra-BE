package com.example.movra.bc.account.infrastructure.user.security.oauth.handler;

import com.example.movra.bc.account.domain.user.type.OauthProvider;
import com.example.movra.bc.account.infrastructure.user.security.auth.AuthDetails;
import com.example.movra.bc.account.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.movra.bc.account.infrastructure.user.security.oauth.OauthAttribute;
import com.example.movra.bc.account.infrastructure.user.security.oauth.dto.PendingOauth;
import com.example.movra.bc.account.infrastructure.user.security.oauth.pending.PendingOauthStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final PendingOauthStore pendingOauthStore;

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
        String accessToken = jwtTokenProvider.generateAccessToken(authDetails.getUser().getId().id());
        String refreshToken = jwtTokenProvider.generateRefreshToken(authDetails.getUser().getId().id());

        jwtTokenProvider.save(authDetails.getUser().getId().id().toString(), refreshToken);

        addHttpOnlyCookie(response, "accessToken", accessToken, 60 * 60);
        addHttpOnlyCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("isProfileCompleted", true)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private void handleNewUser(HttpServletResponse response, OAuth2User oAuth2User) throws IOException {
        String email = oAuth2User.getAttribute(OauthAttribute.EMAIL.getKey());
        OauthProvider provider = OauthProvider.valueOf(oAuth2User.getAttribute(OauthAttribute.PROVIDER.getKey()));

        String pendingToken = pendingOauthStore.save(
                PendingOauth.builder()
                        .email(email)
                        .oauthProvider(provider)
                        .build()
        );

        addHttpOnlyCookie(response, "pendingToken", pendingToken, 60 * 30);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("isProfileCompleted", false)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private void addHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
