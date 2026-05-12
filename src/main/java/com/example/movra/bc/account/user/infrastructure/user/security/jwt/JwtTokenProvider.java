package com.example.movra.bc.account.user.infrastructure.user.security.jwt;

import com.example.movra.bc.account.user.infrastructure.user.security.jwt.exception.ExpiredJwtException;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.exception.InvalidJwtException;
import com.example.movra.bc.account.user.infrastructure.user.security.token.RefreshToken;
import com.example.movra.bc.account.user.infrastructure.user.security.token.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String ACCESS_TOKEN_TYPE = "access_token";
    private static final String REFRESH_TOKEN_TYPE = "refresh_token";

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public void save(String userId, String refreshToken){
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId)
                        .refreshToken(hashToken(refreshToken))
                        .ttl(jwtProperties.refreshExp())
                        .build()
        );
    }

    public Optional<String> findByRefreshToken(String refreshToken){
        if (refreshToken == null || refreshToken.isBlank()) {
            return Optional.empty();
        }

        return refreshTokenRepository.findByRefreshToken(hashToken(refreshToken))
                .map(rt -> refreshToken);
    }

    public String generateAccessToken(UUID userId){
        return generateToken(userId.toString(), ACCESS_TOKEN_TYPE, jwtProperties.accessExp());
    }

    public String generateRefreshToken(UUID userId){
        return generateToken(userId.toString(), REFRESH_TOKEN_TYPE, jwtProperties.refreshExp());
    }

    public String extractAccessTokenSubject(String token) {
        return extractSubject(token, ACCESS_TOKEN_TYPE);
    }

    public String extractRefreshTokenSubject(String token) {
        return extractSubject(token, REFRESH_TOKEN_TYPE);
    }

    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(jwtProperties.header());
        return resolveToken(bearerToken);
    }

    public String resolveToken(String bearerToken) {
        return parseToken(bearerToken);
    }

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(String userId, String type, Long exp){
        return Jwts.builder()
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .setSubject(userId)
                .claim("type", type)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + exp * 1000))
                .compact();
    }

    private String extractSubject(String token, String expectedType) {
        Claims claims = getClaims(token);
        if (!expectedType.equals(claims.get("type", String.class))) {
            throw new InvalidJwtException();
        }

        return claims.getSubject();
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new ExpiredJwtException();
        } catch (Exception e) {
            throw new InvalidJwtException();
        }
    }

    private String parseToken(String bearerToken){
        if(bearerToken != null && bearerToken.startsWith(jwtProperties.prefix())){
            return bearerToken.substring(jwtProperties.prefix().length()).trim();
        }

        return null;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
