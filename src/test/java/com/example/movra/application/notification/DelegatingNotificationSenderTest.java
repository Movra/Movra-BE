package com.example.movra.application.notification;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.infrastructure.DelegatingNotificationSender;
import com.example.movra.sharedkernel.notification.NotificationChannelSender;
import com.example.movra.sharedkernel.notification.NotificationDeliveryException;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

class DelegatingNotificationSenderTest {

    private final UserId userId = UserId.newId();
    private final NotificationPayload payload = NotificationPayload.of(
            NotificationType.D_DAY,
            "D-7",
            "Exam is in 7 days.",
            Map.of("examScheduleId", "exam-schedule-id")
    );

    @Test
    @DisplayName("send dispatches notification to all configured channels")
    void send_withChannels_dispatchesToAllChannels() {
        NotificationChannelSender first = mock(NotificationChannelSender.class);
        NotificationChannelSender second = mock(NotificationChannelSender.class);
        DelegatingNotificationSender sender = new DelegatingNotificationSender(List.of(first, second));

        sender.send(userId, payload);

        then(first).should().send(userId, payload);
        then(second).should().send(userId, payload);
    }

    @Test
    @DisplayName("send succeeds when at least one channel sends successfully")
    void send_oneChannelFails_succeedsWhenOtherChannelSends() {
        NotificationChannelSender failed = mock(NotificationChannelSender.class);
        NotificationChannelSender sent = mock(NotificationChannelSender.class);
        willThrow(new NotificationDeliveryException("channel failed"))
                .given(failed)
                .send(userId, payload);
        DelegatingNotificationSender sender = new DelegatingNotificationSender(List.of(failed, sent));

        sender.send(userId, payload);

        then(failed).should().send(userId, payload);
        then(sent).should().send(userId, payload);
    }

    @Test
    @DisplayName("send throws when all channels fail")
    void send_allChannelsFail_throwsException() {
        NotificationChannelSender first = mock(NotificationChannelSender.class);
        NotificationChannelSender second = mock(NotificationChannelSender.class);
        willThrow(new NotificationDeliveryException("first failed"))
                .given(first)
                .send(userId, payload);
        willThrow(new NotificationDeliveryException("second failed"))
                .given(second)
                .send(userId, payload);
        DelegatingNotificationSender sender = new DelegatingNotificationSender(List.of(first, second));

        assertThatThrownBy(() -> sender.send(userId, payload))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessage("Notification delivery failed for all channels.");
    }

    @Test
    @DisplayName("send throws when no channel is configured")
    void send_noChannel_throwsException() {
        DelegatingNotificationSender sender = new DelegatingNotificationSender(List.of());

        assertThatThrownBy(() -> sender.send(userId, payload))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessage("No notification channel sender configured.");
    }
}
