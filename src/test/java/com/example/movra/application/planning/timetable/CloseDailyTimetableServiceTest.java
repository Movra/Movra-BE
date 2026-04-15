package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.query.DailyPlanLookupService;
import com.example.movra.bc.planning.daily_plan.application.service.query.TaskCompletionQueryService;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.application.service.CloseDailyTimetableService;
import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.Slot;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.repository.DailyTimetableSummaryRepository;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.type.ClosedBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CloseDailyTimetableServiceTest {

    private CloseDailyTimetableService closeDailyTimetableService;

    @Mock
    private DailyTimetableSummaryRepository dailyTimetableSummaryRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private DailyPlanLookupService dailyPlanLookupService;

    @Mock
    private TaskCompletionQueryService taskCompletionQueryService;

    @Mock
    private Timetable timetable;

    @Mock
    private Slot slotA;

    @Mock
    private Slot slotB;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final UserId userId = UserId.newId();
    private final LocalDate date = LocalDate.of(2026, 4, 14);

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        closeDailyTimetableService = new CloseDailyTimetableService(
                dailyTimetableSummaryRepository, timetableRepository,
                dailyPlanLookupService, taskCompletionQueryService, clock);
    }

    @Test
    @DisplayName("close aggregates task completion from timetable slots")
    void close_aggregatesSlots() {
        // given
        DailyPlanId planId = DailyPlanId.newId();
        TaskId taskA = TaskId.newId();
        TaskId taskB = TaskId.newId();

        given(dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanLookupService.findIdByUserAndDate(userId, date)).willReturn(Optional.of(planId));
        given(timetableRepository.findByDailyPlanId(planId)).willReturn(Optional.of(timetable));
        given(timetable.getSlots()).willReturn(List.of(slotA, slotB));
        given(slotA.getTaskId()).willReturn(taskA);
        given(slotB.getTaskId()).willReturn(taskB);
        given(taskCompletionQueryService.findCompletionByTaskIds(List.of(taskA, taskB)))
                .willReturn(Map.of(taskA, true, taskB, false));

        // when
        closeDailyTimetableService.close(userId, date, ClosedBy.USER_ACTION);

        // then
        ArgumentCaptor<DailyTimetableSummary> captor = ArgumentCaptor.forClass(DailyTimetableSummary.class);
        verify(dailyTimetableSummaryRepository).save(captor.capture());
        DailyTimetableSummary saved = captor.getValue();
        assertThat(saved.getTotalCount()).isEqualTo(2);
        assertThat(saved.getCompletedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("close is idempotent when a summary already exists")
    void close_idempotent() {
        // given
        given(dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(true);

        // when
        closeDailyTimetableService.close(userId, date, ClosedBy.USER_ACTION);

        // then
        verify(dailyTimetableSummaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("close records zero counts when there is no timetable for the date")
    void close_noTimetable() {
        // given
        given(dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanLookupService.findIdByUserAndDate(userId, date)).willReturn(Optional.empty());

        // when
        closeDailyTimetableService.close(userId, date, ClosedBy.SCHEDULER);

        // then
        ArgumentCaptor<DailyTimetableSummary> captor = ArgumentCaptor.forClass(DailyTimetableSummary.class);
        verify(dailyTimetableSummaryRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalCount()).isZero();
        assertThat(captor.getValue().getCompletedCount()).isZero();
    }
}
