package com.example.morva.bc.account.application.user;

import com.example.morva.bc.account.application.user.dto.request.LocalLoginRequest;
import com.example.morva.bc.account.application.user.dto.response.TokenResponse;
import com.example.morva.bc.account.application.user.exception.LoginFailedException;
import com.example.morva.bc.account.domain.user.User;
import com.example.morva.bc.account.domain.user.repository.UserRepository;
import com.example.morva.bc.account.infrastructure.user.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalLoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse login(LocalLoginRequest localLoginRequest){

        User user = userRepository.findByAccountId(localLoginRequest.accountId())
                .orElseThrow(() -> {
                    log.warn("로그인 실패: 존재하지 않는 accountId={}", localLoginRequest.accountId());
                    return new LoginFailedException();
                });

        if(!passwordEncoder.matches(localLoginRequest.password(), user.getPasswordHash())){
            log.warn("로그인 실패: 비밀번호 불일치 accountId={}", localLoginRequest.accountId());
            throw new LoginFailedException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId().id());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().id());

        jwtTokenProvider.save(user.getId().id().toString(), refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
