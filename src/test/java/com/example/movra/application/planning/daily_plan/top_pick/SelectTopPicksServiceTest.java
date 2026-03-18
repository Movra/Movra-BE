package com.example.movra.application.planning.daily_plan.top_pick;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.SelectTopPicksService;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.request.TopPicksRequest;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.exception.CoreSelectedLimitExceededException;
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
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SelectTopPicksServiceTest {

    @InjectMocks
    private SelectTopPicksService selectTopPicksService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    private DailyPlan createDailyPlanWithTask() {
        DailyPlan dailyPlan = DailyPlan.create(UserId.newId(), LocalDate.of(2026, 3, 17));
        dailyPlan.addTask("Top Pick 대상");
        return dailyPlan;
    }

    @Test
    @DisplayName("Top Pick 선택 성공")
    void select_success() {
        // given
        DailyPlan dailyPlan = createDailyPlanWithTask();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID taskId = dailyPlan.getTasks().get(0).getTaskId().id();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.of(dailyPlan));

        // when
        selectTopPicksService.select(new TopPicksRequest(30, "중요한 일"), dailyPlanId, taskId);

        // then
        assertThat(dailyPlan.getTasks().get(0).isTopPicked()).isTrue();
        then(dailyPlanRepository).should().save(dailyPlan);
    }

    @Test
    @DisplayName("Top Pick 3개 초과 시 CoreSelectedLimitExceededException 발생")
    void select_exceedsLimit_throwsException() {
        // given
        DailyPlan dailyPlan = DailyPlan.create(UserId.newId(), LocalDate.of(2026, 3, 17));
        for (int i = 0; i < 4; i++) {
            dailyPlan.addTask("할 일 " + i);
        }
        for (int i = 0; i < 3; i++) {
            dailyPlan.markAsTopPicked(dailyPlan.getTasks().get(i).getTaskId(), 30, "메모");
        }
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID fourthTaskId = dailyPlan.getTasks().get(3).getTaskId().id();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.of(dailyPlan));

        // when & then
        assertThatThrownBy(() -> selectTopPicksService.select(new TopPicksRequest(30, "메모"), dailyPlanId, fourthTaskId))
                .isInstanceOf(CoreSelectedLimitExceededException.class);
    }

    @Test
    @DisplayName("존재하지 않는 DailyPlan에서 Top Pick 선택 시 DailyPlanNotFoundException 발생")
    void select_dailyPlanNotFound_throwsException() {
        // given
        UUID dailyPlanId = UUID.randomUUID();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> selectTopPicksService.select(new TopPicksRequest(30, "메모"), dailyPlanId, UUID.randomUUID()))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 Task를 Top Pick 선택 시 TaskNotFoundException 발생")
    void select_taskNotFound_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlanWithTask();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.of(dailyPlan));

        // when & then
        assertThatThrownBy(() -> selectTopPicksService.select(new TopPicksRequest(30, "메모"), dailyPlanId, UUID.randomUUID()))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
