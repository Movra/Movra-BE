package com.example.movra.application.account.device_token;

import com.example.movra.bc.account.device_token.application.exception.DeviceTokenNotFoundException;
import com.example.movra.bc.account.device_token.application.service.UnregisterDeviceTokenService;
import com.example.movra.bc.account.device_token.application.service.dto.request.UnregisterDeviceTokenRequest;
import com.example.movra.bc.account.device_token.domain.DeviceToken;
import com.example.movra.bc.account.device_token.domain.repository.DeviceTokenRepository;
import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnregisterDeviceTokenServiceTest {

    @InjectMocks
    private UnregisterDeviceTokenService unregisterDeviceTokenService;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-14T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final UserId userId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("unregister deletes the token owned by the current user")
    void unregister_success() {
        // given
        givenCurrentUser();
        DeviceToken owned = DeviceToken.register(userId, "token-abc", "iPhone", clock);
        given(deviceTokenRepository.findByToken("token-abc")).willReturn(Optional.of(owned));

        // when
        unregisterDeviceTokenService.unregister(new UnregisterDeviceTokenRequest("token-abc"));

        // then
        verify(deviceTokenRepository).deleteByToken("token-abc");
    }

    @Test
    @DisplayName("unregister throws when the token does not exist")
    void unregister_notFound_throws() {
        // given
        givenCurrentUser();
        given(deviceTokenRepository.findByToken("missing")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> unregisterDeviceTokenService.unregister(new UnregisterDeviceTokenRequest("missing")))
                .isInstanceOf(DeviceTokenNotFoundException.class);
        verify(deviceTokenRepository, never()).deleteByToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("unregister throws when the token belongs to another user")
    void unregister_ownedByOtherUser_throws() {
        // given
        givenCurrentUser();
        UserId otherUser = UserId.newId();
        DeviceToken owned = DeviceToken.register(otherUser, "token-abc", "iPhone", clock);
        given(deviceTokenRepository.findByToken("token-abc")).willReturn(Optional.of(owned));

        // when & then
        assertThatThrownBy(() -> unregisterDeviceTokenService.unregister(new UnregisterDeviceTokenRequest("token-abc")))
                .isInstanceOf(DeviceTokenNotFoundException.class);
        verify(deviceTokenRepository, never()).deleteByToken(org.mockito.ArgumentMatchers.anyString());
    }
}
