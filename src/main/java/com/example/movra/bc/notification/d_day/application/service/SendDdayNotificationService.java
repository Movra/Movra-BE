package com.example.movra.bc.notification.d_day.application.service;

import com.example.movra.bc.notification.application.service.NotificationDeliveryResult;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.notification.d_day.domain.DdayNotificationLog;
import com.example.movra.bc.notification.d_day.domain.repository.DdayNotificationLogRepository;
import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.repository.ExamScheduleRepository;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SendDdayNotificationService {

    private static final Set<Integer> D_DAY_MILESTONES = Set.of(30, 7, 1);

    private final ExamScheduleRepository examScheduleRepository;
    private final DdayNotificationLogRepository ddayNotificationLogRepository;
    private final NotificationGateway notificationGateway;
    private final Clock clock;

    @Transactional
    public int sendDueNotifications() {
        LocalDate today = LocalDate.now(clock);
        List<LocalDate> dueDates = D_DAY_MILESTONES.stream()
                .map(today::plusDays)
                .toList();

        int sentCount = 0;
        for (ExamSchedule examSchedule : examScheduleRepository.findAllByExamDateIn(dueDates)) {
            int daysBefore = (int) ChronoUnit.DAYS.between(today, examSchedule.getExamDate());
            if (!D_DAY_MILESTONES.contains(daysBefore)) {
                continue;
            }
            if (ddayNotificationLogRepository.existsByExamScheduleIdAndMilestoneDays(
                    examSchedule.getExamScheduleId(),
                    daysBefore
            )) {
                continue;
            }

            NotificationDeliveryResult result = notificationGateway.sendSafely(
                    examSchedule.getUserId(),
                    payload(examSchedule, daysBefore)
            );
            if (result == NotificationDeliveryResult.SENT) {
                ddayNotificationLogRepository.save(DdayNotificationLog.sent(
                        examSchedule.getUserId(),
                        examSchedule.getExamScheduleId(),
                        daysBefore,
                        today,
                        clock.instant()
                ));
                sentCount++;
            }
        }

        return sentCount;
    }

    private NotificationPayload payload(ExamSchedule examSchedule, int daysBefore) {
        return NotificationPayload.of(
                NotificationType.D_DAY,
                "D-" + daysBefore + " exam reminder",
                examSchedule.getTitle() + " is coming up. Start with one focus block today.",
                Map.of(
                        "examScheduleId", examSchedule.getExamScheduleId().id().toString(),
                        "examType", examSchedule.getExamType().name(),
                        "examDate", examSchedule.getExamDate().toString(),
                        "daysBefore", String.valueOf(daysBefore)
                )
        );
    }
}
