package com.example.movra.bc.planning.daily_plan.application.service.task.morning;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.request.MindSweepRequest;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AddMorningTaskService {

    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;
    private final NotificationGateway notificationGateway;

    @Transactional
    public void create(MindSweepRequest request, LocalDate targetDate) {
        UserId userId = currentUserQuery.currentUser().userId();

        DailyPlan dailyPlan = dailyPlanRepository.findByUserIdAndPlanDate(userId, targetDate)
                .orElseGet(() -> DailyPlan.create(userId, targetDate));

        Task task = dailyPlan.addMorningTask(request.content());
        dailyPlanRepository.save(dailyPlan);

        analyticsEventRecorder.recordSafely(
                userId,
                AnalyticsEventType.MORNING_TASK_CREATED,
                Map.of(
                        "dailyPlanId", dailyPlan.getDailyPlanId().id().toString(),
                        "taskId", task.getTaskId().id().toString(),
                        "targetDate", targetDate.toString()
                )
        );

        notificationGateway.sendSafely(
                userId,
                NotificationPayload.of(
                        NotificationType.DAILY_FOCUS,
                        "오늘의 집중 준비",
                        "아침 계획이 추가됐어요.",
                        Map.of(
                                "dailyPlanId", dailyPlan.getDailyPlanId().id().toString(),
                                "taskId", task.getTaskId().id().toString(),
                                "targetDate", targetDate.toString()
                        )
                )
        );
    }
}
