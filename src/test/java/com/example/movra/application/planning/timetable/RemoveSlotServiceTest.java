package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.application.service.RemoveSlotService;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.SlotNotFoundException;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
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
class RemoveSlotServiceTest {

    @InjectMocks
    private RemoveSlotService removeSlotService;

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
    @DisplayName("슬롯 삭제 성공")
    void remove_success() {
        // given
        Timetable timetable = Timetable.create(DailyPlanId.newId(), 0);
        timetable.assignTopPick(TaskId.newId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        UUID timetableId = timetable.getTimetableId().id();
        UUID slotId = timetable.getSlots().get(0).getSlotId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable);

        // when
        removeSlotService.remove(timetableId, slotId);

        // then
        assertThat(timetable.getSlots()).isEmpty();
        then(timetableRepository).should().save(timetable);
    }

    @Test
    @DisplayName("존재하지 않는 Timetable에서 슬롯 삭제 시 TimetableNotFoundException 발생")
    void remove_timetableNotFound_throwsException() {
        // given
        UUID timetableId = UUID.randomUUID();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> removeSlotService.remove(timetableId, UUID.randomUUID()))
                .isInstanceOf(TimetableNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 슬롯 삭제 시 SlotNotFoundException 발생")
    void remove_slotNotFound_throwsException() {
        // given
        Timetable timetable = Timetable.create(DailyPlanId.newId(), 0);
        timetable.assignTopPick(TaskId.newId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        UUID timetableId = timetable.getTimetableId().id();
        given(timetableRepository.findById(TimetableId.of(timetableId))).willReturn(Optional.of(timetable));
        stubOwnership(timetable);

        // when & then
        assertThatThrownBy(() -> removeSlotService.remove(timetableId, UUID.randomUUID()))
                .isInstanceOf(SlotNotFoundException.class);
    }
}
