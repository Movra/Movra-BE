package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.timetable.application.service.AddDirectSlotService;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AddDirectSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AddDirectSlotServiceTest {

    @InjectMocks
    private AddDirectSlotService addDirectSlotService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @Test
    @DisplayName("직접 슬롯 추가 성공 - Task 생성 및 슬롯 배정")
    void execute_success() {
        // given
        DailyPlan dailyPlan = DailyPlan.create(UserId.newId(), LocalDate.of(2026, 3, 17));
        DailyPlanId dailyPlanId = dailyPlan.getDailyPlanId();
        Timetable timetable = Timetable.create(dailyPlanId, 0);
        UUID timetableUuid = timetable.getTimetableId().id();

        given(dailyPlanRepository.findById(dailyPlanId)).willReturn(Optional.of(dailyPlan));
        given(timetableRepository.findById(TimetableId.of(timetableUuid))).willReturn(Optional.of(timetable));

        // when
        addDirectSlotService.execute(timetableUuid, dailyPlanId.id(),
                new AddDirectSlotRequest("직접 추가 할 일", LocalTime.of(14, 0), LocalTime.of(15, 0)));

        // then
        assertThat(dailyPlan.getTasks()).hasSize(1);
        assertThat(dailyPlan.getTasks().get(0).getContent()).isEqualTo("직접 추가 할 일");
        assertThat(timetable.getSlots()).hasSize(1);
        assertThat(timetable.getSlots().get(0).isTopPick()).isFalse();
        then(dailyPlanRepository).should().save(dailyPlan);
        then(timetableRepository).should().save(timetable);
    }

    @Test
    @DisplayName("존재하지 않는 DailyPlan으로 직접 슬롯 추가 시 DailyPlanNotFoundException 발생")
    void execute_dailyPlanNotFound_throwsException() {
        // given
        UUID dailyPlanId = UUID.randomUUID();
        given(dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addDirectSlotService.execute(UUID.randomUUID(), dailyPlanId,
                new AddDirectSlotRequest("할 일", LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 Timetable로 직접 슬롯 추가 시 TimetableNotFoundException 발생")
    void execute_timetableNotFound_throwsException() {
        // given
        DailyPlan dailyPlan = DailyPlan.create(UserId.newId(), LocalDate.of(2026, 3, 17));
        DailyPlanId dailyPlanId = dailyPlan.getDailyPlanId();
        UUID timetableId = UUID.randomUUID();

        given(dailyPlanRepository.findById(dailyPlanId)).willReturn(Optional.of(dailyPlan));
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> addDirectSlotService.execute(timetableId, dailyPlanId.id(),
                new AddDirectSlotRequest("할 일", LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TimetableNotFoundException.class);
    }
}
