package com.example.movra.application.notification;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.account.user.infrastructure.user.device.DeviceToken;
import com.example.movra.bc.account.user.infrastructure.user.device.repository.DeviceTokenRepository;
import com.example.movra.bc.notification.infrastructure.FcmNotificationDeliveryException;
import com.example.movra.bc.notification.infrastructure.FcmNotificationSender;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import com.google.firebase.ErrorCode;
import com.google.firebase.FirebaseException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FcmNotificationSenderTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    private FcmNotificationSender sender;

    private final UserId userId = UserId.newId();
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-29T07:30:00Z"), ZoneId.of("Asia/Seoul"));
    private final NotificationPayload payload = NotificationPayload.of(
            NotificationType.DAILY_FOCUS,
            "Daily focus",
            "Start with five minutes.",
            Map.of("focusSessionId", "focus-session-id")
    );

    @BeforeEach
    void setUp() {
        sender = new FcmNotificationSender(firebaseMessaging, deviceTokenRepository);
    }

    @Test
    @DisplayName("send sends an FCM message to each registered device token")
    void send_withDeviceToken_sendsFcmMessage() throws Exception {
        DeviceToken deviceToken = deviceToken("token-1");
        given(deviceTokenRepository.findAllByUserId(userId)).willReturn(List.of(deviceToken));
        given(firebaseMessaging.send(any(Message.class))).willReturn("message-id");

        sender.send(userId, payload);

        then(firebaseMessaging).should().send(any(Message.class));
        then(deviceTokenRepository).should(never()).deleteByToken(any());
    }

    @Test
    @DisplayName("send throws when target user has no registered device token")
    void send_noDeviceToken_throwsException() {
        given(deviceTokenRepository.findAllByUserId(userId)).willReturn(List.of());

        assertThatThrownBy(() -> sender.send(userId, payload))
                .isInstanceOf(FcmNotificationDeliveryException.class);

        then(firebaseMessaging).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("send deletes unregistered token and throws when all FCM sends fail")
    void send_allDeviceTokensFail_deletesUnregisteredTokenAndThrowsException() throws Exception {
        DeviceToken deviceToken = deviceToken("expired-token");
        FirebaseMessagingException exception = messagingException(MessagingErrorCode.UNREGISTERED);
        given(deviceTokenRepository.findAllByUserId(userId)).willReturn(List.of(deviceToken));
        willThrow(exception).given(firebaseMessaging).send(any(Message.class));

        assertThatThrownBy(() -> sender.send(userId, payload))
                .isInstanceOf(FcmNotificationDeliveryException.class)
                .hasCause(exception);

        then(deviceTokenRepository).should().deleteByToken("expired-token");
    }

    private DeviceToken deviceToken(String token) {
        return DeviceToken.register(userId, token, "android", clock);
    }

    private FirebaseMessagingException messagingException(MessagingErrorCode messagingErrorCode) throws Exception {
        FirebaseException firebaseException = new FirebaseException(ErrorCode.NOT_FOUND, "FCM failed.", null);
        Method method = FirebaseMessagingException.class.getDeclaredMethod(
                "withMessagingErrorCode",
                FirebaseException.class,
                MessagingErrorCode.class
        );
        method.setAccessible(true);
        return (FirebaseMessagingException) method.invoke(null, firebaseException, messagingErrorCode);
    }
}
