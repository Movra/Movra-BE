package com.example.movra.application.planning.daily_plan.mind_sweep;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.QueryMindSweepService;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.response.MindSweepResponse;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QueryMindSweepServiceTest {

    @InjectMocks
    private QueryMindSweepService queryMindSweepService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    @BeforeEach
    void setUp() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build());
    }

    @Test
    @DisplayName("Mind Sweep Task 목록 조회 성공")
    void queryAll_success() {
        // given
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
        dailyPlan.addTask("할 일 1");
        dailyPlan.addTask("할 일 2");
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId)).willReturn(Optional.of(dailyPlan));

        // when
        List<MindSweepResponse> responses = queryMindSweepService.queryAll(dailyPlanId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).content()).isEqualTo("할 일 1");
        assertThat(responses.get(1).content()).isEqualTo("할 일 2");
    }

    @Test
    @DisplayName("Top Pick된 Task는 Mind Sweep 목록에서 제외")
    void queryAll_excludesTopPicked() {
        // given
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
        dailyPlan.addTask("일반 할 일");
        dailyPlan.addTask("Top Pick 할 일");
        dailyPlan.markAsTopPicked(dailyPlan.getTasks().get(1).getTaskId(), 30, "메모");
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId)).willReturn(Optional.of(dailyPlan));

        // when
        List<MindSweepResponse> responses = queryMindSweepService.queryAll(dailyPlanId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).content()).isEqualTo("일반 할 일");
    }

    @Test
    @DisplayName("존재하지 않는 DailyPlan 조회 시 DailyPlanNotFoundException 발생")
    void queryAll_dailyPlanNotFound_throwsException() {
        // given
        UUID dailyPlanId = UUID.randomUUID();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> queryMindSweepService.queryAll(dailyPlanId))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }
}
