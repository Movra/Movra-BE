package com.example.movra.bc.planning.daily_plan.application.service.task.top_pick;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.request.TopPicksRequest;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SelectTopPicksService {

    private final DailyPlanRepository dailyPlanRepository;
    private final BehaviorProfileRepository behaviorProfileRepository;
    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;
    private final NotificationGateway notificationGateway;

    @Transactional
    public void select(TopPicksRequest request, UUID dailyPlanId, UUID taskId){
        UserId userId = currentUserQuery.currentUser().userId();

        DailyPlan dailyPlan = dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId)
                .orElseThrow(DailyPlanNotFoundException::new);

        int maxTopPicks = behaviorProfileRepository.findByUserId(userId)
                .map(BehaviorProfile::getExecutionDifficulty)
                .map(difficulty -> difficulty.getMaxTopPicks())
                .orElse(DailyPlan.DEFAULT_MAX_TOP_PICKS);

        boolean selected = dailyPlan.markAsTopPicked(TaskId.of(taskId), request.estimatedMinutes(), request.memo(), maxTopPicks);

        dailyPlanRepository.save(dailyPlan);

        if (selected) {
            analyticsEventRecorder.recordSafely(
                    userId,
                    AnalyticsEventType.TOP_PICK_SELECTED,
                    Map.of(
                            "dailyPlanId", dailyPlanId.toString(),
                            "taskId", taskId.toString(),
                            "estimatedMinutes", String.valueOf(request.estimatedMinutes())
                    )
            );
            notificationGateway.sendSafely(
                    userId,
                    NotificationPayload.of(
                            NotificationType.DAILY_TOP_PICKS,
                            "Top Pick 선택 완료",
                            "오늘의 핵심 과제가 정해졌어요.",
                            Map.of(
                                    "dailyPlanId", dailyPlanId.toString(),
                                    "taskId", taskId.toString(),
                                    "estimatedMinutes", String.valueOf(request.estimatedMinutes())
                            )
                    )
            );
        }
    }
}
