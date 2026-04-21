package com.example.movra.application.statistics.focus_statistics;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsDataSource;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsStatus;
import com.example.movra.bc.statistics.focus_statistics.application.service.QueryFocusPeriodStatisticsService;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusPeriodStatisticsResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusPeriodStatisticsCalculator;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsPeriodResolver;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsReadPort;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusSessionOverlapCalculator;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSessionView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryView;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class QueryFocusPeriodStatisticsServiceTest {

    @Mock
    private FocusStatisticsReadPort focusStatisticsReadPort;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    private QueryFocusPeriodStatisticsService queryFocusPeriodStatisticsService;

    private final FocusStatisticsPeriodResolver focusStatisticsPeriodResolver = new FocusStatisticsPeriodResolver();
    private final FocusSessionOverlapCalculator focusSessionOverlapCalculator = new FocusSessionOverlapCalculator();
    private final UserId userId = UserId.newId();
    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");

    @BeforeEach
    void setUp() {
        FocusPeriodStatisticsCalculator focusPeriodStatisticsCalculator = new FocusPeriodStatisticsCalculator(
                focusStatisticsReadPort,
                focusStatisticsPeriodResolver,
                focusSessionOverlapCalculator
        );
        queryFocusPeriodStatisticsService = new QueryFocusPeriodStatisticsService(
                currentUserQuery,
                clock,
                focusStatisticsPeriodResolver,
                focusPeriodStatisticsCalculator
        );
    }

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
        lenient().when(clock.getZone()).thenReturn(zoneId);
    }

    @Test
    @DisplayName("queryDaily uses raw sessions for today")
    void queryDaily_today_usesRawSessions() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 12);
        Instant now = ZonedDateTime.of(2026, 4, 12, 10, 0, 0, 0, zoneId).toInstant();
        FocusStatisticsPeriod dayPeriod = focusStatisticsPeriodResolver.resolveDay(targetDate, zoneId);

        FocusStatisticsSessionView crossDaySession = focusSession(
                ZonedDateTime.of(2026, 4, 11, 23, 50, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 12, 0, 10, 0, 0, zoneId).toInstant()
        );
        FocusStatisticsSessionView daytimeSession = focusSession(
                ZonedDateTime.of(2026, 4, 12, 9, 0, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 12, 9, 30, 0, 0, zoneId).toInstant()
        );
        FocusStatisticsSessionView inProgressSession = focusSession(
                ZonedDateTime.of(2026, 4, 12, 9, 45, 0, 0, zoneId).toInstant(),
                null
        );

        given(clock.instant()).willReturn(now);
        given(focusStatisticsReadPort.findSessions(userId, dayPeriod))
                .willReturn(List.of(crossDaySession, daytimeSession, inProgressSession));

        FocusPeriodStatisticsResponse response = queryFocusPeriodStatisticsService.queryDaily(targetDate);

        assertThat(response.totalFocusSeconds()).isEqualTo(3300L);
        assertThat(response.averageDailyFocusSeconds()).isEqualTo(3300L);
        assertThat(response.coveredDayCount()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.PARTIAL);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.RAW);
        then(focusStatisticsReadPort).should(never()).findSummaryRange(eq(userId), eq(targetDate), eq(targetDate));
    }

    @Test
    @DisplayName("queryDaily uses summaries for closed days")
    void queryDaily_past_usesSummaries() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 10);
        Instant now = ZonedDateTime.of(2026, 4, 12, 10, 0, 0, 0, zoneId).toInstant();

        given(clock.instant()).willReturn(now);
        given(focusStatisticsReadPort.findSummaryRange(userId, targetDate, targetDate))
                .willReturn(List.of(summary(targetDate, 5400L, 2)));

        FocusPeriodStatisticsResponse response = queryFocusPeriodStatisticsService.queryDaily(targetDate);

        assertThat(response.totalFocusSeconds()).isEqualTo(5400L);
        assertThat(response.averageDailyFocusSeconds()).isEqualTo(5400L);
        assertThat(response.coveredDayCount()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.FINAL);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.SUMMARY);
        then(focusStatisticsReadPort).should(never()).findSessions(eq(userId), eq(focusStatisticsPeriodResolver.resolveDay(targetDate, zoneId)));
    }

    @Test
    @DisplayName("queryDaily falls back to raw sessions when a closed-day summary is missing")
    void queryDaily_past_fallsBackToRaw() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 10);
        Instant now = ZonedDateTime.of(2026, 4, 12, 10, 0, 0, 0, zoneId).toInstant();
        FocusStatisticsPeriod dayPeriod = focusStatisticsPeriodResolver.resolveDay(targetDate, zoneId);

        FocusStatisticsSessionView firstSession = focusSession(
                ZonedDateTime.of(2026, 4, 10, 9, 0, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 10, 10, 0, 0, 0, zoneId).toInstant()
        );
        FocusStatisticsSessionView secondSession = focusSession(
                ZonedDateTime.of(2026, 4, 10, 13, 0, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 10, 13, 30, 0, 0, zoneId).toInstant()
        );

        given(clock.instant()).willReturn(now);
        given(focusStatisticsReadPort.findSummaryRange(userId, targetDate, targetDate))
                .willReturn(List.of());
        given(focusStatisticsReadPort.findSessions(userId, dayPeriod))
                .willReturn(List.of(firstSession, secondSession));

        FocusPeriodStatisticsResponse response = queryFocusPeriodStatisticsService.queryDaily(targetDate);

        assertThat(response.totalFocusSeconds()).isEqualTo(5400L);
        assertThat(response.averageDailyFocusSeconds()).isEqualTo(5400L);
        assertThat(response.coveredDayCount()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.FINAL);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.RAW);
    }

    @Test
    @DisplayName("queryWeekly combines closed summaries with today's raw sessions")
    void queryWeekly_currentWeek_combinesSummaryAndRaw() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 15);
        LocalDate today = LocalDate.of(2026, 4, 15);
        Instant now = ZonedDateTime.of(2026, 4, 15, 12, 0, 0, 0, zoneId).toInstant();
        FocusStatisticsPeriod todayPeriod = focusStatisticsPeriodResolver.resolveDay(today, zoneId);

        FocusStatisticsSessionView todayMorningSession = focusSession(
                ZonedDateTime.of(2026, 4, 15, 9, 0, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 15, 10, 0, 0, 0, zoneId).toInstant()
        );
        FocusStatisticsSessionView todayInProgressSession = focusSession(
                ZonedDateTime.of(2026, 4, 15, 11, 30, 0, 0, zoneId).toInstant(),
                null
        );

        given(clock.instant()).willReturn(now);
        given(focusStatisticsReadPort.findSummaryRange(userId, LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 14)))
                .willReturn(List.of(
                        summary(LocalDate.of(2026, 4, 13), 3600L, 1),
                        summary(LocalDate.of(2026, 4, 14), 7200L, 2)
                ));
        given(focusStatisticsReadPort.findSessions(userId, todayPeriod))
                .willReturn(List.of(todayMorningSession, todayInProgressSession));

        FocusPeriodStatisticsResponse response = queryFocusPeriodStatisticsService.queryWeekly(targetDate);

        assertThat(response.totalFocusSeconds()).isEqualTo(16200L);
        assertThat(response.averageDailyFocusSeconds()).isEqualTo(5400L);
        assertThat(response.coveredDayCount()).isEqualTo(3);
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.PARTIAL);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.MIXED);
    }

    @Test
    @DisplayName("queryWeekly groups contiguous missing closed days into a single raw range")
    void queryWeekly_missingClosedDays_queriesSingleRawRange() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 15);
        Instant now = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, zoneId).toInstant();
        FocusStatisticsPeriod missingRange = focusStatisticsPeriodResolver.resolveRange(
                LocalDate.of(2026, 4, 14),
                LocalDate.of(2026, 4, 16),
                zoneId
        );

        FocusStatisticsSessionView missingRangeSession = focusSession(
                ZonedDateTime.of(2026, 4, 14, 9, 0, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 16, 10, 0, 0, 0, zoneId).toInstant()
        );

        given(clock.instant()).willReturn(now);
        given(focusStatisticsReadPort.findSummaryRange(userId, LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 19)))
                .willReturn(List.of(
                        summary(LocalDate.of(2026, 4, 13), 3600L, 1),
                        summary(LocalDate.of(2026, 4, 17), 1800L, 1),
                        summary(LocalDate.of(2026, 4, 18), 2400L, 1),
                        summary(LocalDate.of(2026, 4, 19), 1200L, 1)
                ));
        given(focusStatisticsReadPort.findSessions(userId, missingRange))
                .willReturn(List.of(missingRangeSession));

        FocusPeriodStatisticsResponse response = queryFocusPeriodStatisticsService.queryWeekly(targetDate);

        assertThat(response.totalFocusSeconds()).isEqualTo(185400L);
        assertThat(response.averageDailyFocusSeconds()).isEqualTo(26485L);
        assertThat(response.coveredDayCount()).isEqualTo(7);
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.FINAL);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.MIXED);
        then(focusStatisticsReadPort).should().findSummaryRange(
                eq(userId),
                eq(LocalDate.of(2026, 4, 13)),
                eq(LocalDate.of(2026, 4, 19))
        );
        then(focusStatisticsReadPort).should().findSessions(eq(userId), eq(missingRange));
    }

    @Test
    @DisplayName("queryDaily returns future-empty metadata for future dates")
    void queryDaily_future_returnsFutureEmptyMetadata() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 13);
        Instant now = ZonedDateTime.of(2026, 4, 12, 10, 0, 0, 0, zoneId).toInstant();

        given(clock.instant()).willReturn(now);

        FocusPeriodStatisticsResponse response = queryFocusPeriodStatisticsService.queryDaily(targetDate);

        assertThat(response.totalFocusSeconds()).isZero();
        assertThat(response.averageDailyFocusSeconds()).isZero();
        assertThat(response.coveredDayCount()).isZero();
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.FUTURE_EMPTY);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.NONE);
        then(focusStatisticsReadPort).should(never()).findSummaryRange(eq(userId), eq(targetDate), eq(targetDate));
        then(focusStatisticsReadPort).should(never()).findSessions(eq(userId), eq(focusStatisticsPeriodResolver.resolveDay(targetDate, zoneId)));
    }

    @Test
    @DisplayName("queryDaily uses configured app zone for future boundary")
    void queryDaily_usesConfiguredAppZoneForFutureBoundary() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 12);
        Instant now = Instant.parse("2026-04-11T15:30:00Z");
        ZoneId appZone = ZoneId.of("UTC");

        given(clock.instant()).willReturn(now);
        given(clock.getZone()).willReturn(appZone);

        FocusPeriodStatisticsResponse response = queryFocusPeriodStatisticsService.queryDaily(targetDate);

        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.FUTURE_EMPTY);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.NONE);
        assertThat(response.totalFocusSeconds()).isZero();
        then(focusStatisticsReadPort).should(never()).findSummaryRange(eq(userId), eq(targetDate), eq(targetDate));
        then(focusStatisticsReadPort).should(never()).findSessions(eq(userId), eq(focusStatisticsPeriodResolver.resolveDay(targetDate, appZone)));
    }

    private FocusStatisticsSummaryView summary(LocalDate date, long totalSeconds, int sessionCount) {
        return new FocusStatisticsSummaryView(date, totalSeconds, List.of());
    }

    private FocusStatisticsSessionView focusSession(Instant startedAt, Instant endedAt) {
        return new FocusStatisticsSessionView(startedAt, endedAt);
    }
}
