package com.example.movra.application.notification.d_day;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.application.service.NotificationDeliveryResult;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.notification.d_day.application.service.SendDdayNotificationService;
import com.example.movra.bc.notification.d_day.domain.DdayNotificationLog;
import com.example.movra.bc.notification.d_day.domain.repository.DdayNotificationLogRepository;
import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.repository.ExamScheduleRepository;
import com.example.movra.bc.planning.exam_schedule.domain.type.ExamType;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SendDdayNotificationServiceTest {

    @Mock
    private ExamScheduleRepository examScheduleRepository;

    @Mock
    private DdayNotificationLogRepository ddayNotificationLogRepository;

    @Mock
    private NotificationGateway notificationGateway;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-04-29T07:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final LocalDate today = LocalDate.of(2026, 4, 29);
    private final UserId userId = UserId.newId();

    private SendDdayNotificationService sendDdayNotificationService;

    @BeforeEach
    void setUp() {
        sendDdayNotificationService = new SendDdayNotificationService(
                examScheduleRepository,
                ddayNotificationLogRepository,
                notificationGateway,
                clock
        );
    }

    @Test
    @DisplayName("sendDueNotifications sends D-Day notification and records milestone log")
    void sendDueNotifications_dueExam_sendsAndLogs() {
        ExamSchedule examSchedule = schedule(ExamType.SUNUNG, "Sunung", today.plusDays(30));
        given(examScheduleRepository.findAllByExamDateIn(argThat(this::containsDdayMilestones)))
                .willReturn(List.of(examSchedule));
        given(ddayNotificationLogRepository.existsByExamScheduleIdAndMilestoneDays(
                examSchedule.getExamScheduleId(),
                30
        )).willReturn(false);
        given(notificationGateway.sendSafely(
                eq(userId),
                argThat(payload -> matchesPayload(payload, examSchedule, 30))
        )).willReturn(NotificationDeliveryResult.SENT);

        int sentCount = sendDdayNotificationService.sendDueNotifications();

        assertThat(sentCount).isEqualTo(1);
        then(ddayNotificationLogRepository).should().save(argThat(log ->
                log.getUserId().equals(userId)
                        && log.getExamScheduleId().equals(examSchedule.getExamScheduleId())
                        && log.getMilestoneDays() == 30
                        && log.getSentDate().equals(today)
                        && log.getSentAt().equals(clock.instant())
        ));
    }

    @Test
    @DisplayName("sendDueNotifications skips already sent exam milestone")
    void sendDueNotifications_alreadySent_skips() {
        ExamSchedule examSchedule = schedule(ExamType.NAESIN, "Midterm", today.plusDays(7));
        given(examScheduleRepository.findAllByExamDateIn(any())).willReturn(List.of(examSchedule));
        given(ddayNotificationLogRepository.existsByExamScheduleIdAndMilestoneDays(
                examSchedule.getExamScheduleId(),
                7
        )).willReturn(true);

        int sentCount = sendDdayNotificationService.sendDueNotifications();

        assertThat(sentCount).isZero();
        then(notificationGateway).shouldHaveNoInteractions();
        then(ddayNotificationLogRepository).should(never()).save(any(DdayNotificationLog.class));
    }

    @Test
    @DisplayName("sendDueNotifications does not record log when gateway skips delivery")
    void sendDueNotifications_gatewaySkipped_doesNotLog() {
        ExamSchedule examSchedule = schedule(ExamType.HAKPYUNG, "Mock exam", today.plusDays(1));
        given(examScheduleRepository.findAllByExamDateIn(any())).willReturn(List.of(examSchedule));
        given(ddayNotificationLogRepository.existsByExamScheduleIdAndMilestoneDays(
                examSchedule.getExamScheduleId(),
                1
        )).willReturn(false);
        given(notificationGateway.sendSafely(eq(userId), any(NotificationPayload.class)))
                .willReturn(NotificationDeliveryResult.SKIPPED_PREFERENCE_DISABLED);

        int sentCount = sendDdayNotificationService.sendDueNotifications();

        assertThat(sentCount).isZero();
        then(ddayNotificationLogRepository).should(never()).save(any(DdayNotificationLog.class));
    }

    private boolean containsDdayMilestones(Collection<LocalDate> dueDates) {
        return dueDates.contains(today.plusDays(30))
                && dueDates.contains(today.plusDays(7))
                && dueDates.contains(today.plusDays(1));
    }

    private boolean matchesPayload(NotificationPayload payload, ExamSchedule examSchedule, int daysBefore) {
        return payload.type() == NotificationType.D_DAY
                && payload.title().contains("D-" + daysBefore)
                && payload.data().get("examScheduleId").equals(examSchedule.getExamScheduleId().id().toString())
                && payload.data().get("examType").equals(examSchedule.getExamType().name())
                && payload.data().get("examDate").equals(examSchedule.getExamDate().toString())
                && payload.data().get("daysBefore").equals(String.valueOf(daysBefore));
    }

    private ExamSchedule schedule(ExamType examType, String title, LocalDate examDate) {
        return ExamSchedule.create(userId, examType, title, examDate, null, clock);
    }
}
