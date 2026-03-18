package com.example.movra.application.planning.daily_plan.mind_sweep;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.CompleteMindSweepService;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.exception.TaskNotFoundException;
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

@ExtendWith(MockitoExtension.class)
class CompleteMindSweepServiceTest {

    @InjectMocks
    private CompleteMindSweepService completeMindSweepService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    private DailyPlan createDailyPlanWithTask() {
        DailyPlan dailyPlan = DailyPlan.create(UserId.newId(), LocalDate.of(2026, 3, 17));
        dailyPlan.addTask("완료할 할 일");
        return dailyPlan;
    }

    @Test
    @DisplayName("Mind Sweep Task 완료 성공")
    void complete_success() {
        // given
        DailyPlan dailyPlan = createDailyPlanWithTask();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID taskId = dailyPlan.getTasks().get(0).getTaskId().id();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.of(dailyPlan));

        // when
        completeMindSweepService.complete(dailyPlanId, taskId);

        // then
        assertThat(dailyPlan.getTasks().get(0).isCompleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 DailyPlan의 Task 완료 시 DailyPlanNotFoundException 발생")
    void complete_dailyPlanNotFound_throwsException() {
        // given
        UUID dailyPlanId = UUID.randomUUID();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> completeMindSweepService.complete(dailyPlanId, UUID.randomUUID()))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 Task 완료 시 TaskNotFoundException 발생")
    void complete_taskNotFound_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlanWithTask();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.of(dailyPlan));

        // when & then
        assertThatThrownBy(() -> completeMindSweepService.complete(dailyPlanId, UUID.randomUUID()))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
