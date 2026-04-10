package com.example.movra.application.planning.daily_plan.event;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.event.SlotRescheduledEventHandler;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.exception.InvalidTopPickEstimatedMinutesException;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.timetable.domain.event.SlotRescheduledEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SlotRescheduledEventHandlerTest {

    @InjectMocks
    private SlotRescheduledEventHandler slotRescheduledEventHandler;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Test
    @DisplayName("슬롯 시간 재조정 시 Top Pick 예상 시간을 함께 갱신한다")
    void handle_success() {
        // given
        DailyPlan dailyPlan = createDailyPlanWithTopPick();
        Task task = dailyPlan.getTasks().get(0);
        SlotRescheduledEvent event = new SlotRescheduledEvent(
                dailyPlan.getDailyPlanId(),
                task.getTaskId(),
                45
        );
        given(dailyPlanRepository.findById(dailyPlan.getDailyPlanId())).willReturn(Optional.of(dailyPlan));

        // when
        slotRescheduledEventHandler.handle(event);

        // then
        assertThat(task.getTopPickDetail().getEstimatedMinutes()).isEqualTo(45);
        then(dailyPlanRepository).should().save(dailyPlan);
    }

    @Test
    @DisplayName("슬롯 시간 재조정 시 예상 시간이 0 이하이면 InvalidTopPickEstimatedMinutesException 발생")
    void handle_invalidEstimatedMinutes_throwsException() {
        // given
        DailyPlan dailyPlan = createDailyPlanWithTopPick();
        Task task = dailyPlan.getTasks().get(0);
        SlotRescheduledEvent event = new SlotRescheduledEvent(
                dailyPlan.getDailyPlanId(),
                task.getTaskId(),
                0
        );
        given(dailyPlanRepository.findById(dailyPlan.getDailyPlanId())).willReturn(Optional.of(dailyPlan));

        // when & then
        assertThatThrownBy(() -> slotRescheduledEventHandler.handle(event))
                .isInstanceOf(InvalidTopPickEstimatedMinutesException.class);
    }

    private DailyPlan createDailyPlanWithTopPick() {
        DailyPlan dailyPlan = DailyPlan.create(UserId.newId(), LocalDate.of(2026, 4, 10));
        Task task = dailyPlan.addTask("집중 작업");
        dailyPlan.markAsTopPicked(task.getTaskId(), 30, "중요 작업");
        return dailyPlan;
    }
}
