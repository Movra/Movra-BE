package com.example.movra.application.planning.daily_plan.top_pick;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.DeselectTopPicksService;
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
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DeselectTopPicksServiceTest {

    @InjectMocks
    private DeselectTopPicksService deselectTopPicksService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    private DailyPlan createDailyPlanWithTopPick() {
        DailyPlan dailyPlan = DailyPlan.create(UserId.newId(), LocalDate.of(2026, 3, 17));
        dailyPlan.addTask("Top Pick된 할 일");
        dailyPlan.markAsTopPicked(dailyPlan.getTasks().get(0).getTaskId(), 30, "메모");
        return dailyPlan;
    }

    @Test
    @DisplayName("Top Pick 해제 성공")
    void deselect_success() {
        // given
        DailyPlan dailyPlan = createDailyPlanWithTopPick();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        UUID taskId = dailyPlan.getTasks().get(0).getTaskId().id();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.of(dailyPlan));

        // when
        deselectTopPicksService.deselect(dailyPlanId, taskId);

        // then
        assertThat(dailyPlan.getTasks().get(0).isTopPicked()).isFalse();
        then(dailyPlanRepository).should().save(dailyPlan);
    }

    @Test
    @DisplayName("존재하지 않는 DailyPlan에서 Top Pick 해제 시 DailyPlanNotFoundException 발생")
    void deselect_dailyPlanNotFound_throwsException() {
        // given
        UUID dailyPlanId = UUID.randomUUID();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deselectTopPicksService.deselect(dailyPlanId, UUID.randomUUID()))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 Task를 Top Pick 해제 시 TaskNotFoundException 발생")
    void deselect_taskNotFound_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlanWithTopPick();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.of(dailyPlan));

        // when & then
        assertThatThrownBy(() -> deselectTopPicksService.deselect(dailyPlanId, UUID.randomUUID()))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
