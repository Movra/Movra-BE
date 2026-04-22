package com.example.movra.application.planning.daily_plan.top_pick;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreference;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.SelectTopPicksService;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.request.TopPicksRequest;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.exception.InvalidTopPickEstimatedMinutesException;
import com.example.movra.bc.planning.daily_plan.domain.exception.InvalidTopPickMemoException;
import com.example.movra.bc.planning.daily_plan.domain.exception.TaskNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.exception.TopPickLimitExceededException;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SelectTopPicksServiceTest {

    @InjectMocks
    private SelectTopPicksService selectTopPicksService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private BehaviorProfileRepository behaviorProfileRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    @BeforeEach
    void setUp() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
        lenient().when(behaviorProfileRepository.findByUserId(userId))
                .thenReturn(Optional.empty());
    }

    private DailyPlan createDailyPlanWithTask() {
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
        dailyPlan.addTask("Top Pick target");
        return dailyPlan;
    }

    @Test
    @DisplayName("select succeeds")
    void select_success() {
        DailyPlan dailyPlan = createDailyPlanWithTask();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID taskId = dailyPlan.getTasks().get(0).getTaskId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId))
                .willReturn(Optional.of(dailyPlan));

        selectTopPicksService.select(new TopPicksRequest(30, "Important"), dailyPlanId, taskId);

        assertThat(dailyPlan.getTasks().get(0).isTopPicked()).isTrue();
        then(dailyPlanRepository).should().save(dailyPlan);
    }

    @Test
    @DisplayName("select throws when top pick limit is exceeded")
    void select_exceedsLimit_throwsException() {
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
        for (int i = 0; i < 4; i++) {
            dailyPlan.addTask("Task " + i);
        }
        for (int i = 0; i < 3; i++) {
            dailyPlan.markAsTopPicked(dailyPlan.getTasks().get(i).getTaskId(), 30, "Memo", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        }
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID fourthTaskId = dailyPlan.getTasks().get(3).getTaskId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId))
                .willReturn(Optional.of(dailyPlan));

        assertThatThrownBy(() -> selectTopPicksService.select(
                new TopPicksRequest(30, "Memo"),
                dailyPlanId,
                fourthTaskId
        )).isInstanceOf(TopPickLimitExceededException.class);
    }

    @Test
    @DisplayName("select throws when estimated minutes are invalid")
    void select_invalidEstimatedMinutes_throwsException() {
        DailyPlan dailyPlan = createDailyPlanWithTask();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID taskId = dailyPlan.getTasks().get(0).getTaskId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId))
                .willReturn(Optional.of(dailyPlan));

        assertThatThrownBy(() -> selectTopPicksService.select(
                new TopPicksRequest(0, "Memo"),
                dailyPlanId,
                taskId
        )).isInstanceOf(InvalidTopPickEstimatedMinutesException.class);
    }

    @Test
    @DisplayName("select throws when memo is blank")
    void select_blankMemo_throwsException() {
        DailyPlan dailyPlan = createDailyPlanWithTask();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID taskId = dailyPlan.getTasks().get(0).getTaskId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId))
                .willReturn(Optional.of(dailyPlan));

        assertThatThrownBy(() -> selectTopPicksService.select(
                new TopPicksRequest(30, " "),
                dailyPlanId,
                taskId
        )).isInstanceOf(InvalidTopPickMemoException.class);
    }

    @Test
    @DisplayName("select throws when memo is too long")
    void select_tooLongMemo_throwsException() {
        DailyPlan dailyPlan = createDailyPlanWithTask();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID taskId = dailyPlan.getTasks().get(0).getTaskId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId))
                .willReturn(Optional.of(dailyPlan));

        assertThatThrownBy(() -> selectTopPicksService.select(
                new TopPicksRequest(30, "a".repeat(256)),
                dailyPlanId,
                taskId
        )).isInstanceOf(InvalidTopPickMemoException.class);
    }

    @Test
    @DisplayName("select throws when daily plan is missing")
    void select_dailyPlanNotFound_throwsException() {
        UUID dailyPlanId = UUID.randomUUID();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> selectTopPicksService.select(
                new TopPicksRequest(30, "Memo"),
                dailyPlanId,
                UUID.randomUUID()
        )).isInstanceOf(DailyPlanNotFoundException.class);
    }

    @Test
    @DisplayName("select throws when task is missing")
    void select_taskNotFound_throwsException() {
        DailyPlan dailyPlan = createDailyPlanWithTask();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId))
                .willReturn(Optional.of(dailyPlan));

        assertThatThrownBy(() -> selectTopPicksService.select(
                new TopPicksRequest(30, "Memo"),
                dailyPlanId,
                UUID.randomUUID()
        )).isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("LOW difficulty limits top picks to 1")
    void select_lowDifficulty_limitedTo1() {
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
        dailyPlan.addTask("Task 0");
        dailyPlan.addTask("Task 1");
        dailyPlan.markAsTopPicked(dailyPlan.getTasks().get(0).getTaskId(), 30, "Memo", 1);
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID secondTaskId = dailyPlan.getTasks().get(1).getTaskId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId))
                .willReturn(Optional.of(dailyPlan));
        BehaviorProfile profile = BehaviorProfile.create(
                userId, ExecutionDifficulty.LOW, SocialPreference.MEDIUM,
                RecoveryStyle.QUICK_RESTART, 9, 18, CoachingMode.NEUTRAL
        );
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(profile));

        assertThatThrownBy(() -> selectTopPicksService.select(
                new TopPicksRequest(30, "Memo"),
                dailyPlanId,
                secondTaskId
        )).isInstanceOf(TopPickLimitExceededException.class);
    }

    @Test
    @DisplayName("No behavior profile uses default limit of 3")
    void select_noBehaviorProfile_defaultLimit3() {
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
        for (int i = 0; i < 4; i++) {
            dailyPlan.addTask("Task " + i);
        }
        for (int i = 0; i < 2; i++) {
            dailyPlan.markAsTopPicked(dailyPlan.getTasks().get(i).getTaskId(), 30, "Memo", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        }
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID thirdTaskId = dailyPlan.getTasks().get(2).getTaskId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId))
                .willReturn(Optional.of(dailyPlan));
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.empty());

        selectTopPicksService.select(new TopPicksRequest(30, "Memo"), dailyPlanId, thirdTaskId);

        assertThat(dailyPlan.getTasks().get(2).isTopPicked()).isTrue();
    }
}
