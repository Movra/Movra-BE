package com.example.movra.application.planning.daily_plan.morning;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.request.MindSweepRequest;
import com.example.movra.bc.planning.daily_plan.application.service.task.morning.AddMorningTaskService;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AddMorningTaskServiceTest {

    @InjectMocks
    private AddMorningTaskService addMorningTaskService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    @Mock
    private NotificationGateway notificationGateway;

    private final UserId userId = UserId.newId();
    private final LocalDate targetDate = LocalDate.of(2026, 4, 29);

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("create adds morning task and records analytics event")
    void create_success() {
        givenCurrentUser();
        DailyPlan dailyPlan = DailyPlan.create(userId, targetDate);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, targetDate))
                .willReturn(Optional.of(dailyPlan));

        addMorningTaskService.create(new MindSweepRequest("Morning task"), targetDate);

        assertThat(dailyPlan.getMorningTasks()).hasSize(1);
        then(dailyPlanRepository).should().save(dailyPlan);
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.MORNING_TASK_CREATED),
                argThat(properties ->
                        properties.get("dailyPlanId").equals(dailyPlan.getDailyPlanId().id().toString())
                                && properties.get("taskId").equals(dailyPlan.getMorningTasks().get(0).getTaskId().id().toString())
                                && properties.get("targetDate").equals(targetDate.toString())
                )
        );
        then(notificationGateway).should().sendSafely(
                eq(userId),
                argThat(payload ->
                        payload.type().name().equals("DAILY_FOCUS")
                                && payload.data().get("taskId").equals(dailyPlan.getMorningTasks().get(0).getTaskId().id().toString())
                )
        );
    }
}
