package com.example.morva.bc.account.infrastructure.user.security.jwt;

import com.example.morva.bc.account.infrastructure.user.security.jwt.exception.ExpiredJwtException;
import com.example.morva.bc.account.infrastructure.user.security.jwt.exception.InvalidJwtException;
import com.example.morva.bc.account.infrastructure.user.security.token.RefreshToken;
import com.example.morva.bc.account.infrastructure.user.security.token.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public void save(String userId, String refreshToken){
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId)
                        .refreshToken(refreshToken)
                        .ttl(jwtProperties.refreshExp())
                        .build()
        );
    }

    public Optional<String> findByRefreshToken(String refreshToken){
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .map(RefreshToken::getRefreshToken);
    }

    public String generateAccessToken(UUID userId){
        return generateToken(userId.toString(), "access_token", jwtProperties.accessExp());
    }

    public String generateRefreshToken(UUID userId){
        return generateToken(userId.toString(), "refresh_token", jwtProperties.refreshExp());
    }

    public String extractSubject(String token) {
        return getClaims(token).getSubject();
    }

    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(jwtProperties.header());
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
            return bearerToken.replace(jwtProperties.prefix(), "").trim();
        }

        return null;
    }
}
