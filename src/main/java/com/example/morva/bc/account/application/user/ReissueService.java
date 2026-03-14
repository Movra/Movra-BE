package com.example.morva.bc.account.application.user;

import com.example.morva.bc.account.application.user.dto.request.TokenReissueRequest;
import com.example.morva.bc.account.application.user.dto.response.TokenResponse;
import com.example.morva.bc.account.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.morva.bc.account.application.user.exception.RefreshTokenNotFoundException;
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

        UUID userId = UUID.fromString(jwtTokenProvider.extractSubject(refreshToken));

        return TokenResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(userId))
                .refreshToken(jwtTokenProvider.generateRefreshToken(userId))
                .build();
    }
}
