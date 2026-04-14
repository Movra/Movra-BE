package com.example.movra.sharedkernel.notification;

import com.example.movra.bc.account.device_token.domain.DeviceToken;
import com.example.movra.bc.account.device_token.domain.repository.DeviceTokenRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmNotificationSenderTest {

    @InjectMocks
    private FcmNotificationSender fcmNotificationSender;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private BatchResponse batchResponse;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-14T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final UserId userId = UserId.newId();

    @Test
    @DisplayName("send multicasts message to all registered tokens of the user")
    void send_withTokens_invokesMulticast() throws Exception {
        // given
        DeviceToken token1 = DeviceToken.register(userId, "token-1", "iPhone", clock);
        DeviceToken token2 = DeviceToken.register(userId, "token-2", "Android", clock);
        given(deviceTokenRepository.findAllByUserId(userId)).willReturn(List.of(token1, token2));
        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willReturn(batchResponse);
        given(batchResponse.getFailureCount()).willReturn(0);

        NotificationPayload payload = NotificationPayload.of(
                NotificationType.DAILY_FOCUS,
                "오늘의 집중",
                "3시간 25분 집중",
                Map.of("date", "2026-04-14")
        );

        // when
        fcmNotificationSender.send(userId, payload);

        // then
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        verify(firebaseMessaging).sendEachForMulticast(captor.capture());
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    @DisplayName("send skips multicast when user has no device tokens")
    void send_noTokens_skips() throws Exception {
        // given
        given(deviceTokenRepository.findAllByUserId(userId)).willReturn(List.of());

        NotificationPayload payload = NotificationPayload.of(
                NotificationType.DAILY_FOCUS, "t", "b", Map.of()
        );

        // when
        fcmNotificationSender.send(userId, payload);

        // then
        verify(firebaseMessaging, never()).sendEachForMulticast(any(MulticastMessage.class));
    }
}
