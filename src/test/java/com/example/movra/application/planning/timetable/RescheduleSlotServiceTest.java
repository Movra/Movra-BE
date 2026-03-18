package com.example.movra.application.planning.timetable;

import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.application.service.RescheduleSlotService;
import com.example.movra.bc.planning.timetable.application.service.dto.request.RescheduleSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.InvalidTimeRangeException;
import com.example.movra.bc.planning.timetable.domain.exception.SlotNotFoundException;
import com.example.movra.bc.planning.timetable.domain.exception.TimeOverlapException;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RescheduleSlotServiceTest {

    @InjectMocks
    private RescheduleSlotService rescheduleSlotService;

    @Mock
    private TimetableRepository timetableRepository;

    private Timetable createTimetableWithSlot() {
        Timetable timetable = Timetable.create(DailyPlanId.newId(), 0);
        timetable.assignTopPick(TaskId.newId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        return timetable;
    }

    @Test
    @DisplayName("슬롯 시간 변경 성공")
    void reschedule_success() {
        // given
        Timetable timetable = createTimetableWithSlot();
        UUID timetableId = timetable.getTimetableId().id();
        UUID slotId = timetable.getSlots().get(0).getSlotId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));

        // when
        rescheduleSlotService.reschedule(timetableId, slotId,
                new RescheduleSlotRequest(LocalTime.of(14, 0), LocalTime.of(15, 0)));

        // then
        assertThat(timetable.getSlots().get(0).getStartTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(timetable.getSlots().get(0).getEndTime()).isEqualTo(LocalTime.of(15, 0));
        then(timetableRepository).should().save(timetable);
    }

    @Test
    @DisplayName("존재하지 않는 Timetable 시간 변경 시 TimetableNotFoundException 발생")
    void reschedule_timetableNotFound_throwsException() {
        // given
        UUID timetableId = UUID.randomUUID();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> rescheduleSlotService.reschedule(timetableId, UUID.randomUUID(),
                new RescheduleSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TimetableNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 Slot 시간 변경 시 SlotNotFoundException 발생")
    void reschedule_slotNotFound_throwsException() {
        // given
        Timetable timetable = createTimetableWithSlot();
        UUID timetableId = timetable.getTimetableId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));

        // when & then
        assertThatThrownBy(() -> rescheduleSlotService.reschedule(timetableId, UUID.randomUUID(),
                new RescheduleSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(SlotNotFoundException.class);
    }

    @Test
    @DisplayName("다른 슬롯과 시간 겹침 시 TimeOverlapException 발생")
    void reschedule_timeOverlap_throwsException() {
        // given
        Timetable timetable = createTimetableWithSlot();
        timetable.assignTopPick(TaskId.newId(), LocalTime.of(11, 0), LocalTime.of(12, 0));
        UUID timetableId = timetable.getTimetableId().id();
        UUID slotId = timetable.getSlots().get(0).getSlotId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));

        // when & then
        assertThatThrownBy(() -> rescheduleSlotService.reschedule(timetableId, slotId,
                new RescheduleSlotRequest(LocalTime.of(11, 0), LocalTime.of(12, 0))))
                .isInstanceOf(TimeOverlapException.class);
    }

    @Test
    @DisplayName("시작 시간이 종료 시간보다 늦으면 InvalidTimeRangeException 발생")
    void reschedule_invalidTimeRange_throwsException() {
        // given
        Timetable timetable = createTimetableWithSlot();
        UUID timetableId = timetable.getTimetableId().id();
        UUID slotId = timetable.getSlots().get(0).getSlotId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));

        // when & then
        assertThatThrownBy(() -> rescheduleSlotService.reschedule(timetableId, slotId,
                new RescheduleSlotRequest(LocalTime.of(15, 0), LocalTime.of(14, 0))))
                .isInstanceOf(InvalidTimeRangeException.class);
    }
}
