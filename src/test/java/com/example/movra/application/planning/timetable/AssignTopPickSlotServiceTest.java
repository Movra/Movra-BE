package com.example.movra.application.planning.timetable;

import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.timetable.application.service.AssignTopPickSlotService;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AssignTopPickSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.InvalidTimeRangeException;
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
class AssignTopPickSlotServiceTest {

    @InjectMocks
    private AssignTopPickSlotService assignTopPickSlotService;

    @Mock
    private TimetableRepository timetableRepository;

    private Timetable createTimetable() {
        return Timetable.create(DailyPlanId.newId(), 0);
    }

    @Test
    @DisplayName("TopPick 슬롯 배정 성공")
    void assign_success() {
        // given
        Timetable timetable = createTimetable();
        UUID timetableId = timetable.getTimetableId().id();
        UUID taskId = UUID.randomUUID();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));

        // when
        assignTopPickSlotService.assign(timetableId, taskId,
                new AssignTopPickSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0)));

        // then
        assertThat(timetable.getSlots()).hasSize(1);
        assertThat(timetable.getSlots().get(0).isTopPick()).isTrue();
        assertThat(timetable.getSlots().get(0).getTaskId()).isEqualTo(TaskId.of(taskId));
        then(timetableRepository).should().save(timetable);
    }

    @Test
    @DisplayName("존재하지 않는 Timetable에 TopPick 배정 시 TimetableNotFoundException 발생")
    void assign_timetableNotFound_throwsException() {
        // given
        UUID timetableId = UUID.randomUUID();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> assignTopPickSlotService.assign(timetableId, UUID.randomUUID(),
                new AssignTopPickSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TimetableNotFoundException.class);
    }

    @Test
    @DisplayName("시간 겹침 시 TimeOverlapException 발생")
    void assign_timeOverlap_throwsException() {
        // given
        Timetable timetable = createTimetable();
        UUID timetableId = timetable.getTimetableId().id();
        timetable.assignTopPick(TaskId.newId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));

        // when & then
        assertThatThrownBy(() -> assignTopPickSlotService.assign(timetableId, UUID.randomUUID(),
                new AssignTopPickSlotRequest(LocalTime.of(9, 30), LocalTime.of(10, 30))))
                .isInstanceOf(TimeOverlapException.class);
    }

    @Test
    @DisplayName("시작 시간이 종료 시간보다 늦으면 InvalidTimeRangeException 발생")
    void assign_invalidTimeRange_throwsException() {
        // given
        Timetable timetable = createTimetable();
        UUID timetableId = timetable.getTimetableId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));

        // when & then
        assertThatThrownBy(() -> assignTopPickSlotService.assign(timetableId, UUID.randomUUID(),
                new AssignTopPickSlotRequest(LocalTime.of(10, 0), LocalTime.of(9, 0))))
                .isInstanceOf(InvalidTimeRangeException.class);
    }
}
