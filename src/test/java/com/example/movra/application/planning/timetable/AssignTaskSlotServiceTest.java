package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.application.service.AssignTaskSlotService;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AssignTaskSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.exception.TopPicksNotFullyAssignedException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
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
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AssignTaskSlotServiceTest {

    @InjectMocks
    private AssignTaskSlotService assignTaskSlotService;

    @Mock
    private TimetableRepository timetableRepository;

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

    private void stubOwnership(Timetable timetable) {
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(timetable.getDailyPlanId(), userId)).willReturn(Optional.of(dailyPlan));
    }

    @Test
    @DisplayName("일반 Task 슬롯 배정 성공 (TopPick이 없는 경우)")
    void assign_success() {
        // given
        Timetable timetable = Timetable.create(DailyPlanId.newId(), 0);
        UUID timetableId = timetable.getTimetableId().id();
        UUID taskId = UUID.randomUUID();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable);

        // when
        assignTaskSlotService.assign(timetableId, taskId,
                new AssignTaskSlotRequest(LocalTime.of(11, 0), LocalTime.of(12, 0)));

        // then
        assertThat(timetable.getSlots()).hasSize(1);
        assertThat(timetable.getSlots().get(0).isTopPick()).isFalse();
        assertThat(timetable.getSlots().get(0).getTaskId()).isEqualTo(TaskId.of(taskId));
        then(timetableRepository).should().save(timetable);
    }

    @Test
    @DisplayName("TopPick 전체 배정 후 일반 Task 배정 성공")
    void assign_afterTopPicksAssigned_success() {
        // given
        Timetable timetable = Timetable.create(DailyPlanId.newId(), 1);
        timetable.assignTopPick(TaskId.newId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        UUID timetableId = timetable.getTimetableId().id();
        UUID taskId = UUID.randomUUID();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable);

        // when
        assignTaskSlotService.assign(timetableId, taskId,
                new AssignTaskSlotRequest(LocalTime.of(11, 0), LocalTime.of(12, 0)));

        // then
        assertThat(timetable.getSlots()).hasSize(2);
    }

    @Test
    @DisplayName("TopPick 미배정 상태에서 일반 Task 배정 시 TopPicksNotFullyAssignedException 발생")
    void assign_topPicksNotAssigned_throwsException() {
        // given
        Timetable timetable = Timetable.create(DailyPlanId.newId(), 2);
        UUID timetableId = timetable.getTimetableId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable);

        // when & then
        assertThatThrownBy(() -> assignTaskSlotService.assign(timetableId, UUID.randomUUID(),
                new AssignTaskSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TopPicksNotFullyAssignedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 Timetable에 Task 배정 시 TimetableNotFoundException 발생")
    void assign_timetableNotFound_throwsException() {
        // given
        UUID timetableId = UUID.randomUUID();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> assignTaskSlotService.assign(timetableId, UUID.randomUUID(),
                new AssignTaskSlotRequest(LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(TimetableNotFoundException.class);
    }
}
