package com.example.morva.bc.account.infrastructure.user.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.jwt")
public record JwtProperties(
        String secretKey,
        Long accessExp,
        Long refreshExp,
        String header,
        String prefix
) {
    public JwtProperties {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("JWT secretKey는 필수입니다.");
        }
        if (accessExp == null || accessExp <= 0) {
            throw new IllegalArgumentException("JWT accessExp는 0보다 커야 합니다.");
        }
        if (refreshExp == null || refreshExp <= 0) {
            throw new IllegalArgumentException("JWT refreshExp는 0보다 커야 합니다.");
        }
        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException("JWT header는 필수입니다.");
        }
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("JWT prefix는 필수입니다.");
        }
    }
}
