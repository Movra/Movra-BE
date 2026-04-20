package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.timetable.application.service.support.DailyTimetableCloser;
import com.example.movra.bc.planning.timetable.application.service.support.DailyTimetableSummarySaver;
import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.repository.DailyTimetableSummaryRepository;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CloseDailyTimetableServiceTest {

    private DailyTimetableCloser closeDailyTimetableService;

    @Mock
    private DailyTimetableSummaryRepository dailyTimetableSummaryRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private DailyTimetableSummarySaver dailyTimetableSummarySaver;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final UserId userId = UserId.newId();
    private final LocalDate date = LocalDate.of(2026, 4, 14);

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        closeDailyTimetableService = new DailyTimetableCloser(
                dailyTimetableSummaryRepository, timetableRepository, dailyPlanRepository, dailyTimetableSummarySaver, clock
        );
    }

    @Test
    @DisplayName("close snapshots timetable slots and task completion")
    void close_aggregatesSlots() {
        DailyPlan dailyPlan = createDailyPlan();
        Timetable timetable = createTimetable(dailyPlan);

        given(dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, date)).willReturn(Optional.of(dailyPlan));
        given(timetableRepository.findByDailyPlanId(dailyPlan.getDailyPlanId())).willReturn(Optional.of(timetable));
        given(dailyTimetableSummarySaver.save(any())).willReturn(true);

        closeDailyTimetableService.close(userId, date);

        ArgumentCaptor<DailyTimetableSummary> captor = ArgumentCaptor.forClass(DailyTimetableSummary.class);
        verify(dailyTimetableSummarySaver).save(captor.capture());
        DailyTimetableSummary saved = captor.getValue();
        assertThat(saved.getDailyPlanId()).isEqualTo(dailyPlan.getDailyPlanId());
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getTotalCount()).isEqualTo(2);
        assertThat(saved.getCompletedCount()).isEqualTo(1);
        assertThat(saved.getItems()).hasSize(2);
        assertThat(saved.getItems().get(0).getContentSnapshot()).isEqualTo("Top Pick Task");
        assertThat(saved.getItems().get(0).isCompletedSnapshot()).isTrue();
        assertThat(saved.getItems().get(0).getStartTimeSnapshot()).isEqualTo(LocalTime.of(9, 0));
        assertThat(saved.getItems().get(0).getEndTimeSnapshot()).isEqualTo(LocalTime.of(10, 0));
        assertThat(saved.getItems().get(0).isTopPickSnapshot()).isTrue();
        assertThat(saved.getItems().get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(saved.getItems().get(1).getContentSnapshot()).isEqualTo("General Task");
        assertThat(saved.getItems().get(1).isCompletedSnapshot()).isFalse();
        assertThat(saved.getItems().get(1).getStartTimeSnapshot()).isEqualTo(LocalTime.of(11, 0));
        assertThat(saved.getItems().get(1).getEndTimeSnapshot()).isEqualTo(LocalTime.of(12, 0));
        assertThat(saved.getItems().get(1).isTopPickSnapshot()).isFalse();
        assertThat(saved.getItems().get(1).getDisplayOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("close is idempotent when a summary already exists")
    void close_idempotent() {
        given(dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(true);

        closeDailyTimetableService.close(userId, date);

        verify(dailyTimetableSummarySaver, never()).save(any());
    }

    @Test
    @DisplayName("close skips when there is no daily plan")
    void close_noDailyPlan() {
        given(dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, date)).willReturn(Optional.empty());

        closeDailyTimetableService.close(userId, date);

        verify(dailyTimetableSummarySaver, never()).save(any());
    }

    @Test
    @DisplayName("close skips when there is no timetable for the date")
    void close_noTimetable() {
        DailyPlan dailyPlan = DailyPlan.create(userId, date);
        given(dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, date)).willReturn(Optional.of(dailyPlan));
        given(timetableRepository.findByDailyPlanId(dailyPlan.getDailyPlanId())).willReturn(Optional.empty());

        closeDailyTimetableService.close(userId, date);

        verify(dailyTimetableSummarySaver, never()).save(any());
    }

    @Test
    @DisplayName("close treats duplicate writes as idempotent success")
    void close_duplicateAtWrite_isIgnored() {
        DailyPlan dailyPlan = createDailyPlan();
        Timetable timetable = createTimetable(dailyPlan);
        given(dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, date)).willReturn(Optional.of(dailyPlan));
        given(timetableRepository.findByDailyPlanId(dailyPlan.getDailyPlanId())).willReturn(Optional.of(timetable));
        given(dailyTimetableSummarySaver.save(any())).willReturn(false);

        assertThatCode(() -> closeDailyTimetableService.close(userId, date))
                .doesNotThrowAnyException();
    }

    private DailyPlan createDailyPlan() {
        DailyPlan dailyPlan = DailyPlan.create(userId, date);
        var topPickTask = dailyPlan.addTask("Top Pick Task");
        dailyPlan.markAsTopPicked(topPickTask.getTaskId(), 60, "Deep work");
        dailyPlan.completeTask(topPickTask.getTaskId());
        dailyPlan.addTask("General Task");
        return dailyPlan;
    }

    private Timetable createTimetable(DailyPlan dailyPlan) {
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 1);
        var topPickTask = dailyPlan.getTasks().get(0);
        var generalTask = dailyPlan.getTasks().get(1);
        timetable.assignTopPick(topPickTask.getTaskId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        timetable.assignTask(generalTask.getTaskId(), LocalTime.of(11, 0), LocalTime.of(12, 0));
        return timetable;
    }
}
