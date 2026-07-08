package com.example.movra.application.account.user;

import com.example.movra.bc.account.user.application.user.OauthCompleteService;
import com.example.movra.bc.account.user.application.user.dto.request.OauthCompleteRequest;
import com.example.movra.bc.account.user.application.user.dto.response.OauthCompleteResponse;
import com.example.movra.bc.account.user.application.user.exception.OauthCallbackNotFoundException;
import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.type.OauthProvider;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.OauthCallbackPayload;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.dto.PendingOauth;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending.OauthCallbackStore;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.pending.PendingOauthStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OauthCompleteServiceTest {

    @InjectMocks
    private OauthCompleteService oauthCompleteService;

    @Mock
    private OauthCallbackStore oauthCallbackStore;

    @Mock
    private PendingOauthStore pendingOauthStore;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("기존 OAuth 사용자는 callback code를 토큰으로 교환한다")
    void complete_existingUser_returnsTokens() {
        // given
        OauthCompleteRequest request = new OauthCompleteRequest("callback-code");
        User user = User.createOauthUser(
                "testuser",
                "테스트유저",
                "image.png",
                "test@example.com",
                OauthProvider.GOOGLE,
                "encodedPassword"
        );

        given(oauthCallbackStore.consume(request.code()))
                .willReturn(Optional.of(OauthCallbackPayload.existingUser(user.getId().id())));
        given(userRepository.findById(UserId.of(user.getId().id())))
                .willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken(user.getId().id()))
                .willReturn("accessToken");
        given(jwtTokenProvider.generateRefreshToken(user.getId().id()))
                .willReturn("refreshToken");

        // when
        OauthCompleteResponse response = oauthCompleteService.complete(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
        assertThat(response.pendingToken()).isNull();
        assertThat(response.isProfileCompleted()).isTrue();
        verify(jwtTokenProvider).save(user.getId().id().toString(), "refreshToken");
    }

    @Test
    @DisplayName("신규 OAuth 사용자는 callback code를 pendingToken으로 교환한다")
    void complete_newUser_returnsPendingToken() {
        // given
        OauthCompleteRequest request = new OauthCompleteRequest("callback-code");

        given(oauthCallbackStore.consume(request.code()))
                .willReturn(Optional.of(OauthCallbackPayload.newUser("new@example.com", OauthProvider.NAVER)));
        given(pendingOauthStore.save(org.mockito.ArgumentMatchers.any(PendingOauth.class)))
                .willReturn("pending-token");

        // when
        OauthCompleteResponse response = oauthCompleteService.complete(request);

        // then
        assertThat(response.accessToken()).isNull();
        assertThat(response.refreshToken()).isNull();
        assertThat(response.pendingToken()).isEqualTo("pending-token");
        assertThat(response.isProfileCompleted()).isFalse();

        ArgumentCaptor<PendingOauth> pendingOauthCaptor = ArgumentCaptor.forClass(PendingOauth.class);
        verify(pendingOauthStore).save(pendingOauthCaptor.capture());
        assertThat(pendingOauthCaptor.getValue().email()).isEqualTo("new@example.com");
        assertThat(pendingOauthCaptor.getValue().oauthProvider()).isEqualTo(OauthProvider.NAVER);
    }

    @Test
    @DisplayName("callback code가 없거나 만료되면 예외가 발생한다")
    void complete_missingCode_throwsException() {
        // given
        OauthCompleteRequest request = new OauthCompleteRequest("missing-code");
        given(oauthCallbackStore.consume(request.code())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> oauthCompleteService.complete(request))
                .isInstanceOf(OauthCallbackNotFoundException.class);
    }
}
