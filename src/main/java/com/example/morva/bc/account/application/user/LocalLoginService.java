package com.example.morva.bc.account.application.user;

import com.example.morva.bc.account.application.user.dto.request.LocalLoginRequest;
import com.example.morva.bc.account.application.user.dto.response.TokenResponse;
import com.example.morva.bc.account.application.user.exception.AccountNotFoundException;
import com.example.morva.bc.account.application.user.exception.PasswordMismatchException;
import com.example.morva.bc.account.domain.user.User;
import com.example.morva.bc.account.domain.user.repository.UserRepository;
import com.example.morva.bc.account.infrastructure.user.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalLoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse login(LocalLoginRequest localLoginRequest){

        User user = userRepository.findByAccountId(localLoginRequest.accountId())
                .orElseThrow(AccountNotFoundException::new);

        if(!passwordEncoder.matches(localLoginRequest.password(), user.getPasswordHash())){
            throw new PasswordMismatchException();
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
