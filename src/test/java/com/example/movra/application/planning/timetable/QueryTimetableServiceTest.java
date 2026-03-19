package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.timetable.application.service.QueryTimetableService;
import com.example.movra.bc.planning.timetable.application.service.dto.response.TimetableResponse;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
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

@ExtendWith(MockitoExtension.class)
class QueryTimetableServiceTest {

    @InjectMocks
    private QueryTimetableService queryTimetableService;

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

    @Test
    @DisplayName("DailyPlanId로 Timetable 조회 성공")
    void findByDailyPlanId_success() {
        // given
        DailyPlanId dailyPlanId = DailyPlanId.newId();
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));

        Timetable timetable = Timetable.create(dailyPlanId, 0);
        timetable.assignTopPick(
                dailyPlan.addTask("할 일").getTaskId(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        given(timetableRepository.findByDailyPlanId(dailyPlanId)).willReturn(Optional.of(timetable));
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(dailyPlanId, userId)).willReturn(Optional.of(dailyPlan));

        // when
        TimetableResponse response = queryTimetableService.findByDailyPlanId(dailyPlanId.id());

        // then
        assertThat(response.timetableId()).isEqualTo(timetable.getTimetableId().id());
        assertThat(response.dailyPlanId()).isEqualTo(dailyPlanId.id());
        assertThat(response.slots()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 Timetable 조회 시 TimetableNotFoundException 발생")
    void findByDailyPlanId_timetableNotFound_throwsException() {
        // given
        UUID dailyPlanId = UUID.randomUUID();
        given(timetableRepository.findByDailyPlanId(DailyPlanId.of(dailyPlanId))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> queryTimetableService.findByDailyPlanId(dailyPlanId))
                .isInstanceOf(TimetableNotFoundException.class);
    }

    @Test
    @DisplayName("Timetable은 있지만 DailyPlan이 없으면 DailyPlanNotFoundException 발생")
    void findByDailyPlanId_dailyPlanNotFound_throwsException() {
        // given
        DailyPlanId dailyPlanId = DailyPlanId.newId();
        Timetable timetable = Timetable.create(dailyPlanId, 0);
        given(timetableRepository.findByDailyPlanId(dailyPlanId)).willReturn(Optional.of(timetable));
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(dailyPlanId, userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> queryTimetableService.findByDailyPlanId(dailyPlanId.id()))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }
}
