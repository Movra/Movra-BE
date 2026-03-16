package com.example.movra.bc.account.application.user.internal;

import com.example.movra.bc.account.application.user.exception.UserNotFoundException;
import com.example.movra.bc.account.domain.user.User;
import com.example.movra.bc.account.domain.user.repository.UserRepository;
import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.account.infrastructure.user.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public User authenticate(String token){
        String userId = jwtTokenProvider.extractSubject(token);

        try {
            return userRepository.findById(UserId.of(UUID.fromString(userId)))
                    .orElseThrow(UserNotFoundException::new);
        } catch (IllegalArgumentException e) {
            throw new UserNotFoundException();
        }
    }
}
