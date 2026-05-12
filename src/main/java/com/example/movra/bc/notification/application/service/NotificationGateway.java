package com.example.movra.bc.notification.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.domain.NotificationDeliveryLog;
import com.example.movra.bc.notification.domain.NotificationPreference;
import com.example.movra.bc.notification.domain.repository.NotificationDeliveryLogRepository;
import com.example.movra.bc.notification.domain.repository.NotificationPreferenceRepository;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationGateway {

    private static final int SLEEP_START_HOUR = 22;
    private static final int SLEEP_END_HOUR = 7;

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationDeliveryLogRepository notificationDeliveryLogRepository;
    private final NotificationSender notificationSender;
    private final Clock clock;

    @Transactional
    public NotificationDeliveryResult sendSafely(UserId targetUserId, NotificationPayload payload) {
        try {
            return send(targetUserId, payload);
        } catch (RuntimeException e) {
            log.warn("Notification delivery failed. userId={}, type={}", targetUserId.id(), payload.type(), e);
            return NotificationDeliveryResult.FAILED;
        }
    }

    @Transactional
    public NotificationDeliveryResult send(UserId targetUserId, NotificationPayload payload) {
        NotificationPreference preference = notificationPreferenceRepository.findByUserId(targetUserId)
                .orElseGet(() -> NotificationPreference.createDefault(targetUserId));

        if (!preference.allows(payload.type())) {
            return NotificationDeliveryResult.SKIPPED_PREFERENCE_DISABLED;
        }

        LocalDateTime now = LocalDateTime.now(clock);

        if (preference.isSchoolHoursQuietEnabled() && isSchoolHours(now, preference)) {
            return NotificationDeliveryResult.SKIPPED_SCHOOL_HOURS;
        }

        if (isSleepHours(now)) {
            return NotificationDeliveryResult.SKIPPED_SLEEP_HOURS;
        }

        LocalDate today = now.toLocalDate();
        long sentCount = notificationDeliveryLogRepository.countByUserIdAndSentDate(targetUserId, today);
        if (sentCount >= preference.getMaxDailyPushCount()) {
            return NotificationDeliveryResult.SKIPPED_DAILY_LIMIT_EXCEEDED;
        }

        int dailyTypeLimit = payload.type().dailyTypeLimit();
        if (dailyTypeLimit != Integer.MAX_VALUE) {
            long sentTypeCount = notificationDeliveryLogRepository
                    .countByUserIdAndNotificationTypeAndSentDate(targetUserId, payload.type(), today);
            if (sentTypeCount >= dailyTypeLimit) {
                return NotificationDeliveryResult.SKIPPED_TYPE_DAILY_LIMIT_EXCEEDED;
            }
        }

        notificationSender.send(targetUserId, payload);
        notificationDeliveryLogRepository.save(
                NotificationDeliveryLog.sent(targetUserId, payload.type(), today, clock.instant())
        );

        return NotificationDeliveryResult.SENT;
    }

    private boolean isSchoolHours(LocalDateTime now, NotificationPreference preference) {
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        boolean weekday = dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
        if (!weekday && !preference.isWeekendSchoolQuietEnabled()) {
            return false;
        }

        LocalTime time = now.toLocalTime();
        return !time.isBefore(preference.getSchoolHoursStart())
                && time.isBefore(preference.getSchoolHoursEnd());
    }

    private boolean isSleepHours(LocalDateTime now) {
        return now.getHour() >= SLEEP_START_HOUR || now.getHour() < SLEEP_END_HOUR;
    }
}
