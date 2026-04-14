package com.example.movra.application.account.device_token;

import com.example.movra.bc.account.device_token.application.service.RegisterDeviceTokenService;
import com.example.movra.bc.account.device_token.application.service.dto.request.RegisterDeviceTokenRequest;
import com.example.movra.bc.account.device_token.domain.DeviceToken;
import com.example.movra.bc.account.device_token.domain.repository.DeviceTokenRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterDeviceTokenServiceTest {

    @InjectMocks
    private RegisterDeviceTokenService registerDeviceTokenService;

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
    @DisplayName("register saves a new device token when the token does not exist")
    void register_new_savesNewToken() {
        // given
        givenCurrentUser();
        given(deviceTokenRepository.findByToken("token-abc")).willReturn(Optional.empty());
        registerDeviceTokenService = new RegisterDeviceTokenService(deviceTokenRepository, currentUserQuery, clock);

        // when
        registerDeviceTokenService.register(new RegisterDeviceTokenRequest("token-abc", "iPhone"));

        // then
        ArgumentCaptor<DeviceToken> captor = ArgumentCaptor.forClass(DeviceToken.class);
        verify(deviceTokenRepository).save(captor.capture());
        DeviceToken saved = captor.getValue();
        assertThat(saved.getToken()).isEqualTo("token-abc");
        assertThat(saved.getDeviceLabel()).isEqualTo("iPhone");
        assertThat(saved.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("register reassigns an existing token to the current user without inserting a new row")
    void register_existing_reassignsToCurrentUser() {
        // given
        givenCurrentUser();
        UserId previousUserId = UserId.newId();
        DeviceToken existing = DeviceToken.register(previousUserId, "token-abc", "Android", clock);
        given(deviceTokenRepository.findByToken("token-abc")).willReturn(Optional.of(existing));
        registerDeviceTokenService = new RegisterDeviceTokenService(deviceTokenRepository, currentUserQuery, clock);

        // when
        registerDeviceTokenService.register(new RegisterDeviceTokenRequest("token-abc", "iPhone"));

        // then
        assertThat(existing.getUserId()).isEqualTo(userId);
        assertThat(existing.getDeviceLabel()).isEqualTo("iPhone");
        verify(deviceTokenRepository, never()).save(org.mockito.ArgumentMatchers.any(DeviceToken.class));
    }
}
