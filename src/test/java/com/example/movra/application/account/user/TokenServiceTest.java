package com.example.movra.application.account.user;

import com.example.movra.bc.account.user.application.user.exception.UserNotFoundException;
import com.example.movra.bc.account.user.application.user.internal.TokenService;
import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("유효한 토큰으로 인증 시 User 반환")
    void authenticate_success() {
        // given
        UUID userId = UUID.randomUUID();
        String token = "validToken";
        User user = User.createLocalUser(
                "testuser",
                "테스트유저",
                "image.png",
                "test@example.com",
                "encodedPassword"
        );

        given(jwtTokenProvider.extractSubject(token)).willReturn(userId.toString());
        given(userRepository.findById(UserId.of(userId))).willReturn(Optional.of(user));

        // when
        User result = tokenService.authenticate(token);

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 토큰으로 인증 시 UserNotFoundException 발생")
    void authenticate_userNotFound_throwsException() {
        // given
        UUID userId = UUID.randomUUID();
        String token = "validToken";

        given(jwtTokenProvider.extractSubject(token)).willReturn(userId.toString());
        given(userRepository.findById(UserId.of(userId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tokenService.authenticate(token))
                .isInstanceOf(UserNotFoundException.class);
    }
}
