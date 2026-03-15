package com.example.morva.bc.account.application.user;

import com.example.morva.bc.account.application.user.dto.request.TokenReissueRequest;
import com.example.morva.bc.account.application.user.dto.response.TokenResponse;
import com.example.morva.bc.account.application.user.exception.RefreshTokenNotFoundException;
import com.example.morva.bc.account.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.morva.bc.account.infrastructure.user.security.jwt.exception.InvalidJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse reissue(TokenReissueRequest request){
        String refreshToken = jwtTokenProvider.findByRefreshToken(request.refreshToken())
                .orElseThrow(RefreshTokenNotFoundException::new);

        UUID userId;
        try {
            userId = UUID.fromString(jwtTokenProvider.extractSubject(refreshToken));
        } catch (IllegalArgumentException e) {
            throw new InvalidJwtException();
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        jwtTokenProvider.save(userId.toString(), newRefreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
