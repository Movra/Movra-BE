package com.example.morva.application.account.user;

import com.example.morva.bc.account.application.user.LocalLoginService;
import com.example.morva.bc.account.application.user.dto.request.LocalLoginRequest;
import com.example.morva.bc.account.application.user.dto.response.TokenResponse;
import com.example.morva.bc.account.application.user.exception.LoginFailedException;
import com.example.morva.bc.account.domain.user.User;
import com.example.morva.bc.account.domain.user.repository.UserRepository;
import com.example.morva.bc.account.infrastructure.user.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LocalLoginServiceTest {

    @InjectMocks
    private LocalLoginService localLoginService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private User createUser(UUID userId) {
        return User.createLocalUser(
                "testuser",
                "테스트유저",
                "image.png",
                "test@example.com",
                "encodedPassword"
        );
    }

    @Test
    @DisplayName("로그인 성공 시 토큰 반환")
    void login_success() {
        // given
        LocalLoginRequest request = new LocalLoginRequest("testuser", "password123");
        User user = createUser(UUID.randomUUID());

        given(userRepository.findByAccountId(request.accountId())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPasswordHash())).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(user.getId().id())).willReturn("accessToken");
        given(jwtTokenProvider.generateRefreshToken(user.getId().id())).willReturn("refreshToken");

        // when
        TokenResponse response = localLoginService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
    }

    @Test
    @DisplayName("존재하지 않는 accountId로 로그인 시 LoginFailedException 발생")
    void login_accountNotFound_throwsException() {
        // given
        LocalLoginRequest request = new LocalLoginRequest("unknown", "password123");
        given(userRepository.findByAccountId(request.accountId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> localLoginService.login(request))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    @DisplayName("비밀번호 불일치 시 LoginFailedException 발생")
    void login_passwordMismatch_throwsException() {
        // given
        LocalLoginRequest request = new LocalLoginRequest("testuser", "wrongPassword");
        User user = createUser(UUID.randomUUID());

        given(userRepository.findByAccountId(request.accountId())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPasswordHash())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> localLoginService.login(request))
                .isInstanceOf(LoginFailedException.class);
    }
}
