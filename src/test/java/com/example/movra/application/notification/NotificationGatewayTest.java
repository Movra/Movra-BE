package com.example.movra.application.notification;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.application.service.NotificationDeliveryResult;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.notification.domain.NotificationDeliveryLog;
import com.example.movra.bc.notification.domain.NotificationPreference;
import com.example.movra.bc.notification.domain.repository.NotificationDeliveryLogRepository;
import com.example.movra.bc.notification.domain.repository.NotificationPreferenceRepository;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationSender;
import com.example.movra.sharedkernel.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationGatewayTest {

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private NotificationDeliveryLogRepository notificationDeliveryLogRepository;

    @Mock
    private NotificationSender notificationSender;

    private final UserId userId = UserId.newId();
    private final NotificationPayload payload = NotificationPayload.of(
            NotificationType.DAILY_FOCUS,
            "오늘의 집중",
            "5분만 시작해볼까요?",
            Map.of("source", "test")
    );

    @Test
    @DisplayName("send delivers notification and records log when policy allows it")
    void send_allowed_deliversAndRecordsLog() {
        Clock clock = fixedClock("2026-04-29T07:30:00Z"); // 16:30 KST
        NotificationGateway gateway = gateway(clock);
        NotificationPreference preference = enabledPreference();
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(preference));
        given(notificationDeliveryLogRepository.countByUserIdAndSentDate(userId, LocalDate.of(2026, 4, 29)))
                .willReturn(1L);

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SENT);
        then(notificationSender).should().send(userId, payload);
        ArgumentCaptor<NotificationDeliveryLog> captor = ArgumentCaptor.forClass(NotificationDeliveryLog.class);
        then(notificationDeliveryLogRepository).should().save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getNotificationType()).isEqualTo(NotificationType.DAILY_FOCUS);
        assertThat(captor.getValue().getSentDate()).isEqualTo(LocalDate.of(2026, 4, 29));
    }

    @Test
    @DisplayName("send skips when notification type is disabled")
    void send_preferenceDisabled_skips() {
        Clock clock = fixedClock("2026-04-29T07:30:00Z");
        NotificationGateway gateway = gateway(clock);
        given(notificationPreferenceRepository.findByUserId(userId))
                .willReturn(Optional.of(NotificationPreference.createDefault(userId)));

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SKIPPED_PREFERENCE_DISABLED);
        then(notificationSender).should(never()).send(userId, payload);
        then(notificationDeliveryLogRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("send skips during school hours on weekdays")
    void send_schoolHours_skips() {
        Clock clock = fixedClock("2026-04-29T00:30:00Z"); // Wednesday 09:30 KST
        NotificationGateway gateway = gateway(clock);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(enabledPreference()));

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SKIPPED_SCHOOL_HOURS);
        then(notificationSender).should(never()).send(userId, payload);
    }

    @Test
    @DisplayName("send treats 15:29 as school quiet hours")
    void send_schoolHoursUntil1530_skipsBeforeEnd() {
        Clock clock = fixedClock("2026-04-29T06:29:00Z"); // Wednesday 15:29 KST
        NotificationGateway gateway = gateway(clock);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(enabledPreference()));

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SKIPPED_SCHOOL_HOURS);
        then(notificationSender).should(never()).send(userId, payload);
    }

    @Test
    @DisplayName("send allows notifications after school quiet hours end at 15:30")
    void send_afterSchoolHoursEnd_delivers() {
        Clock clock = fixedClock("2026-04-29T06:30:00Z"); // Wednesday 15:30 KST
        NotificationGateway gateway = gateway(clock);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(enabledPreference()));
        given(notificationDeliveryLogRepository.countByUserIdAndSentDate(userId, LocalDate.of(2026, 4, 29)))
                .willReturn(0L);

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SENT);
        then(notificationSender).should().send(userId, payload);
    }

    @Test
    @DisplayName("send does not apply school quiet hours on weekends")
    void send_weekendSchoolHour_delivers() {
        Clock clock = fixedClock("2026-05-02T00:30:00Z"); // Saturday 09:30 KST
        NotificationGateway gateway = gateway(clock);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(enabledPreference()));
        given(notificationDeliveryLogRepository.countByUserIdAndSentDate(userId, LocalDate.of(2026, 5, 2)))
                .willReturn(0L);

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SENT);
        then(notificationSender).should().send(userId, payload);
    }

    @Test
    @DisplayName("send applies school quiet hours on weekends when weekend mute is enabled")
    void send_weekendSchoolHourWithWeekendMute_skips() {
        Clock clock = fixedClock("2026-05-02T00:30:00Z"); // Saturday 09:30 KST
        NotificationGateway gateway = gateway(clock);
        NotificationPreference preference = enabledPreference();
        preference.update(true, true, true, true, true,
                NotificationPreference.DEFAULT_SCHOOL_HOURS_START,
                NotificationPreference.DEFAULT_SCHOOL_HOURS_END,
                true, true, 3);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(preference));

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SKIPPED_SCHOOL_HOURS);
        then(notificationSender).should(never()).send(userId, payload);
    }

    @Test
    @DisplayName("send skips during sleep hours")
    void send_sleepHours_skips() {
        Clock clock = fixedClock("2026-04-29T14:30:00Z"); // 23:30 KST
        NotificationGateway gateway = gateway(clock);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(enabledPreference()));

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SKIPPED_SLEEP_HOURS);
        then(notificationSender).should(never()).send(userId, payload);
    }

    @Test
    @DisplayName("send skips when daily limit is reached")
    void send_dailyLimitReached_skips() {
        Clock clock = fixedClock("2026-04-29T07:30:00Z");
        NotificationGateway gateway = gateway(clock);
        NotificationPreference preference = enabledPreference();
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(preference));
        given(notificationDeliveryLogRepository.countByUserIdAndSentDate(userId, LocalDate.of(2026, 4, 29)))
                .willReturn((long) preference.getMaxDailyPushCount());

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SKIPPED_DAILY_LIMIT_EXCEEDED);
        then(notificationSender).should(never()).send(userId, payload);
    }

    @Test
    @DisplayName("send skips when notification type daily limit is reached")
    void send_typeDailyLimitReached_skips() {
        Clock clock = fixedClock("2026-04-29T07:30:00Z");
        NotificationGateway gateway = gateway(clock);
        NotificationPayload recoveryPayload = NotificationPayload.of(
                NotificationType.RECOVERY,
                "회복",
                "다시 시작해볼까요?",
                Map.of("source", "test")
        );
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(enabledPreference()));
        given(notificationDeliveryLogRepository.countByUserIdAndSentDate(userId, LocalDate.of(2026, 4, 29)))
                .willReturn(0L);
        given(notificationDeliveryLogRepository.countByUserIdAndNotificationTypeAndSentDate(
                userId,
                NotificationType.RECOVERY,
                LocalDate.of(2026, 4, 29)
        )).willReturn(1L);

        NotificationDeliveryResult result = gateway.send(userId, recoveryPayload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SKIPPED_TYPE_DAILY_LIMIT_EXCEEDED);
        then(notificationSender).should(never()).send(userId, recoveryPayload);
    }

    @Test
    @DisplayName("send uses disabled defaults when preference does not exist")
    void send_noPreference_usesDisabledDefaults() {
        Clock clock = fixedClock("2026-04-29T07:30:00Z");
        NotificationGateway gateway = gateway(clock);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.empty());

        NotificationDeliveryResult result = gateway.send(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.SKIPPED_PREFERENCE_DISABLED);
        then(notificationSender).should(never()).send(userId, payload);
    }

    @Test
    @DisplayName("sendSafely returns FAILED when delivery throws")
    void sendSafely_deliveryFailure_returnsFailed() {
        Clock clock = fixedClock("2026-04-29T07:30:00Z");
        NotificationGateway gateway = gateway(clock);
        given(notificationPreferenceRepository.findByUserId(userId)).willReturn(Optional.of(enabledPreference()));
        given(notificationDeliveryLogRepository.countByUserIdAndSentDate(userId, LocalDate.of(2026, 4, 29)))
                .willReturn(0L);
        willThrow(new RuntimeException("push failed"))
                .given(notificationSender)
                .send(userId, payload);

        NotificationDeliveryResult result = gateway.sendSafely(userId, payload);

        assertThat(result).isEqualTo(NotificationDeliveryResult.FAILED);
        then(notificationDeliveryLogRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    private NotificationGateway gateway(Clock clock) {
        return new NotificationGateway(
                notificationPreferenceRepository,
                notificationDeliveryLogRepository,
                notificationSender,
                clock
        );
    }

    private NotificationPreference enabledPreference() {
        NotificationPreference preference = NotificationPreference.createDefault(userId);
        preference.update(
                true,
                true,
                true,
                true,
                true,
                NotificationPreference.DEFAULT_SCHOOL_HOURS_START,
                NotificationPreference.DEFAULT_SCHOOL_HOURS_END,
                false,
                true,
                NotificationPreference.DEFAULT_MAX_DAILY_PUSH_COUNT
        );
        return preference;
    }

    private Clock fixedClock(String instant) {
        return Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
    }
}
