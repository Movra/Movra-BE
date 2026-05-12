package com.example.movra.infrastructure.account.user.security;

import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtProperties;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.exception.InvalidJwtException;
import com.example.movra.bc.account.user.infrastructure.user.security.token.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class JwtTokenProviderTest {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            new JwtProperties(
                    "12345678901234567890123456789012",
                    3600L,
                    7200L,
                    "Authorization",
                    "Bearer "
            ),
            mock(RefreshTokenRepository.class)
    );

    @Test
    void extractAccessTokenSubject_accessToken_returnsSubject() {
        UUID userId = UUID.randomUUID();
        String accessToken = jwtTokenProvider.generateAccessToken(userId);

        String subject = jwtTokenProvider.extractAccessTokenSubject(accessToken);

        assertThat(subject).isEqualTo(userId.toString());
    }

    @Test
    void extractAccessTokenSubject_refreshToken_throwsInvalidJwtException() {
        String refreshToken = jwtTokenProvider.generateRefreshToken(UUID.randomUUID());

        assertThatThrownBy(() -> jwtTokenProvider.extractAccessTokenSubject(refreshToken))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void extractRefreshTokenSubject_accessToken_throwsInvalidJwtException() {
        String accessToken = jwtTokenProvider.generateAccessToken(UUID.randomUUID());

        assertThatThrownBy(() -> jwtTokenProvider.extractRefreshTokenSubject(accessToken))
                .isInstanceOf(InvalidJwtException.class);
    }
}
