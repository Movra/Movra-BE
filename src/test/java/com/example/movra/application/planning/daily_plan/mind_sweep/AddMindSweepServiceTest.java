package com.example.movra.application.planning.daily_plan.mind_sweep;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.AddMindSweepService;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.request.MindSweepRequest;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
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
class AddMindSweepServiceTest {

    @InjectMocks
    private AddMindSweepService addMindSweepService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    private DailyPlan createDailyPlan() {
        return DailyPlan.create(UserId.newId(), LocalDate.of(2026, 3, 17));
    }

    @Test
    @DisplayName("Mind Sweep Task 추가 성공")
    void create_success() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.of(dailyPlan));

        // when
        addMindSweepService.create(new MindSweepRequest("할 일 내용"), dailyPlanId);

        // then
        assertThat(dailyPlan.getTasks()).hasSize(1);
        assertThat(dailyPlan.getTasks().get(0).getContent()).isEqualTo("할 일 내용");
        then(dailyPlanRepository).should().save(dailyPlan);
    }

    @Test
    @DisplayName("존재하지 않는 DailyPlan에 Task 추가 시 DailyPlanNotFoundException 발생")
    void create_dailyPlanNotFound_throwsException() {
        // given
        UUID dailyPlanId = UUID.randomUUID();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addMindSweepService.create(new MindSweepRequest("할 일 내용"), dailyPlanId))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }
}
