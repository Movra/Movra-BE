package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherMonitoringContentReader;
import com.example.movra.bc.focus.focus_session.application.service.support.DailyFocusSummaryReader;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.DailyTopPicksReader;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.timetable.application.service.support.DailyTimetableSummaryReader;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WatcherMonitoringContentReaderTest {

    @Mock
    private DailyFocusSummaryReader dailyFocusSummaryReader;

    @Mock
    private DailyTopPicksReader dailyTopPicksReader;

    @Mock
    private DailyTimetableSummaryReader dailyTimetableSummaryReader;

    @Mock
    private FocusSessionRepository focusSessionRepository;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private TimetableRepository timetableRepository;

    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    private final Clock clock = Clock.fixed(
            ZonedDateTime.of(2026, 4, 20, 10, 30, 0, 0, zoneId).toInstant(),
            zoneId
    );
    private final UserId userId = UserId.newId();
    private final LocalDate date = LocalDate.of(2026, 4, 20);

    private WatcherMonitoringContentReader watcherMonitoringContentReader;

    @BeforeEach
    void setUp() {
        watcherMonitoringContentReader = new WatcherMonitoringContentReader(
                dailyFocusSummaryReader,
                dailyTopPicksReader,
                dailyTimetableSummaryReader,
                focusSessionRepository,
                dailyPlanRepository,
                timetableRepository,
                clock
        );
    }

    @Test
    @DisplayName("findTopPicks falls back to live daily plan when summary is missing")
    void findTopPicks_summaryMissing_returnsLiveTopPicks() {
        DailyPlan dailyPlan = DailyPlan.create(userId, date);
        var topPick = dailyPlan.addTask("수학 문제집");
        dailyPlan.markAsTopPicked(topPick.getTaskId(), 30, "2단원", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        dailyPlan.completeTask(topPick.getTaskId());
        dailyPlan.addTask("일반 작업");
        given(dailyTopPicksReader.findOne(userId, date)).willReturn(Optional.empty());
        given(dailyPlanRepository.findByUserIdAndPlanDateWithTasks(userId, date)).willReturn(Optional.of(dailyPlan));

        var response = watcherMonitoringContentReader.findTopPicks(userId, date);

        assertThat(response).isPresent();
        assertThat(response.get().totalCount()).isEqualTo(1);
        assertThat(response.get().completedCount()).isEqualTo(1);
        assertThat(response.get().items().get(0).content()).isEqualTo("수학 문제집");
        assertThat(response.get().items().get(0).completed()).isTrue();
    }

    @Test
    @DisplayName("findTimetableTasks falls back to live timetable when summary is missing")
    void findTimetableTasks_summaryMissing_returnsLiveTimetable() {
        DailyPlan dailyPlan = DailyPlan.create(userId, date);
        var topPick = dailyPlan.addTask("영어 단어");
        var general = dailyPlan.addTask("국어 지문");
        dailyPlan.markAsTopPicked(topPick.getTaskId(), 30, "day 1", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        dailyPlan.completeTask(topPick.getTaskId());
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 1);
        timetable.assignTopPick(topPick.getTaskId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        timetable.assignTask(general.getTaskId(), LocalTime.of(10, 0), LocalTime.of(11, 0));
        given(dailyTimetableSummaryReader.findOne(userId, date)).willReturn(Optional.empty());
        given(dailyPlanRepository.findByUserIdAndPlanDateWithTasks(userId, date)).willReturn(Optional.of(dailyPlan));
        given(timetableRepository.findByDailyPlanIdWithSlots(dailyPlan.getDailyPlanId())).willReturn(Optional.of(timetable));

        var response = watcherMonitoringContentReader.findTimetableTasks(userId, date);

        assertThat(response).isPresent();
        assertThat(response.get().totalCount()).isEqualTo(2);
        assertThat(response.get().completedCount()).isEqualTo(1);
        assertThat(response.get().items().get(0).contentSnapshot()).isEqualTo("영어 단어");
        assertThat(response.get().items().get(0).completedSnapshot()).isTrue();
        assertThat(response.get().items().get(1).contentSnapshot()).isEqualTo("국어 지문");
    }

    @Test
    @DisplayName("findFocusSessions uses current time as the live current-day boundary")
    void findFocusSessions_currentDay_usesNowAsBoundary() {
        Instant startedAt = ZonedDateTime.of(2026, 4, 20, 10, 0, 0, 0, zoneId).toInstant();
        FocusSession session = FocusSession.start(userId, startedAt);
        given(dailyFocusSummaryReader.findOne(userId, date)).willReturn(Optional.empty());
        given(focusSessionRepository.findAllOverlappingPeriod(any(), any(), any()))
                .willReturn(List.of(session));

        var response = watcherMonitoringContentReader.findFocusSessions(userId, date);

        assertThat(response).isPresent();
        assertThat(response.get().totalSeconds()).isEqualTo(1800L);
        assertThat(response.get().sessionCount()).isEqualTo(1);
        assertThat(response.get().items().get(0).endedAtSnapshot()).isNull();
        assertThat(response.get().items().get(0).overlapEndedAt()).isEqualTo(Instant.now(clock));
    }
}
