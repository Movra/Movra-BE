package com.example.movra.application.statistics.focus_statistics;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsDataSource;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsStatus;
import com.example.movra.bc.statistics.focus_statistics.application.service.QueryFocusTimeOfDayStatisticsService;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimeOfDayStatisticsResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsPeriodResolver;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsReadPort;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsTimeZoneResolver;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusSessionOverlapCalculator;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusTimeBucketCalculator;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSessionView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryItemView;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class QueryFocusTimeOfDayStatisticsServiceTest {

    @Mock
    private FocusStatisticsReadPort focusStatisticsReadPort;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    @Mock
    private FocusStatisticsTimeZoneResolver focusStatisticsTimeZoneResolver;

    private QueryFocusTimeOfDayStatisticsService queryFocusTimeOfDayStatisticsService;

    private final FocusStatisticsPeriodResolver focusStatisticsPeriodResolver = new FocusStatisticsPeriodResolver();
    private final FocusSessionOverlapCalculator focusSessionOverlapCalculator = new FocusSessionOverlapCalculator();
    private final FocusTimeBucketCalculator focusTimeBucketCalculator = new FocusTimeBucketCalculator();
    private final UserId userId = UserId.newId();
    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");

    @BeforeEach
    void setUp() {
        queryFocusTimeOfDayStatisticsService = new QueryFocusTimeOfDayStatisticsService(
                focusStatisticsReadPort,
                currentUserQuery,
                clock,
                focusStatisticsTimeZoneResolver,
                focusStatisticsPeriodResolver,
                focusTimeBucketCalculator,
                focusSessionOverlapCalculator
        );
    }

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
        lenient().when(focusStatisticsTimeZoneResolver.resolve(userId)).thenReturn(zoneId);
    }

    @Test
    @DisplayName("query uses summary items for past dates")
    void query_past_usesSummary() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 10);
        Instant now = ZonedDateTime.of(2026, 4, 12, 10, 0, 0, 0, zoneId).toInstant();

        given(clock.instant()).willReturn(now);
        given(focusStatisticsReadPort.findSummary(userId, targetDate))
                .willReturn(Optional.of(summary(targetDate)));

        FocusTimeOfDayStatisticsResponse response = queryFocusTimeOfDayStatisticsService.query(targetDate);

        assertThat(response.totalFocusSeconds()).isEqualTo(8700L);
        assertThat(response.hourlyBuckets()).hasSize(24);
        assertThat(response.hourlyBuckets().get(0).focusSeconds()).isEqualTo(2400L);
        assertThat(response.hourlyBuckets().get(1).focusSeconds()).isEqualTo(1800L);
        assertThat(response.hourlyBuckets().get(2).focusSeconds()).isEqualTo(3600L);
        assertThat(response.hourlyBuckets().get(3).focusSeconds()).isEqualTo(900L);
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.FINAL);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.SUMMARY);
        then(focusStatisticsReadPort).should(never()).findSessions(eq(userId), eq(focusStatisticsPeriodResolver.resolveDay(targetDate, zoneId)));
    }

    @Test
    @DisplayName("query falls back to raw sessions when a past summary is missing")
    void query_past_fallsBackToRaw() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 10);
        Instant now = ZonedDateTime.of(2026, 4, 12, 10, 0, 0, 0, zoneId).toInstant();
        FocusStatisticsPeriod dayPeriod = focusStatisticsPeriodResolver.resolveDay(targetDate, zoneId);

        FocusStatisticsSessionView firstSession = focusSession(
                ZonedDateTime.of(2026, 4, 10, 0, 20, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 10, 1, 10, 0, 0, zoneId).toInstant()
        );
        FocusStatisticsSessionView secondSession = focusSession(
                ZonedDateTime.of(2026, 4, 10, 1, 40, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 10, 3, 15, 0, 0, zoneId).toInstant()
        );

        given(clock.instant()).willReturn(now);
        given(focusStatisticsReadPort.findSummary(userId, targetDate)).willReturn(Optional.empty());
        given(focusStatisticsReadPort.findSessions(userId, dayPeriod))
                .willReturn(List.of(firstSession, secondSession));

        FocusTimeOfDayStatisticsResponse response = queryFocusTimeOfDayStatisticsService.query(targetDate);

        assertThat(response.totalFocusSeconds()).isEqualTo(8700L);
        assertThat(response.hourlyBuckets().get(0).focusSeconds()).isEqualTo(2400L);
        assertThat(response.hourlyBuckets().get(1).focusSeconds()).isEqualTo(1800L);
        assertThat(response.hourlyBuckets().get(2).focusSeconds()).isEqualTo(3600L);
        assertThat(response.hourlyBuckets().get(3).focusSeconds()).isEqualTo(900L);
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.FINAL);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.RAW);
    }

    @Test
    @DisplayName("query uses raw sessions for today")
    void query_today_usesRaw() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 12);
        Instant now = ZonedDateTime.of(2026, 4, 12, 23, 30, 0, 0, zoneId).toInstant();
        FocusStatisticsPeriod dayPeriod = focusStatisticsPeriodResolver.resolveDay(targetDate, zoneId);

        FocusStatisticsSessionView firstSession = focusSession(
                ZonedDateTime.of(2026, 4, 12, 0, 20, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 12, 1, 10, 0, 0, zoneId).toInstant()
        );
        FocusStatisticsSessionView secondSession = focusSession(
                ZonedDateTime.of(2026, 4, 12, 1, 40, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 12, 3, 15, 0, 0, zoneId).toInstant()
        );

        given(clock.instant()).willReturn(now);
        given(focusStatisticsReadPort.findSessions(userId, dayPeriod))
                .willReturn(List.of(firstSession, secondSession));

        FocusTimeOfDayStatisticsResponse response = queryFocusTimeOfDayStatisticsService.query(targetDate);

        assertThat(response.totalFocusSeconds()).isEqualTo(8700L);
        assertThat(response.hourlyBuckets()).hasSize(24);
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.PARTIAL);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.RAW);
        then(focusStatisticsReadPort).should(never()).findSummary(eq(userId), eq(targetDate));
    }

    @Test
    @DisplayName("query returns empty buckets for future dates")
    void query_future_returnsEmpty() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 13);
        Instant now = ZonedDateTime.of(2026, 4, 12, 10, 0, 0, 0, zoneId).toInstant();

        given(clock.instant()).willReturn(now);

        FocusTimeOfDayStatisticsResponse response = queryFocusTimeOfDayStatisticsService.query(targetDate);

        assertThat(response.totalFocusSeconds()).isZero();
        assertThat(response.hourlyBuckets()).hasSize(24);
        assertThat(response.hourlyBuckets()).allMatch(bucket -> bucket.focusSeconds() == 0L);
        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.FUTURE_EMPTY);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.NONE);
    }

    @Test
    @DisplayName("query uses resolved user zone for future boundary")
    void query_usesResolvedUserZoneForFutureBoundary() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 12);
        Instant now = Instant.parse("2026-04-11T15:30:00Z");
        ZoneId userZone = ZoneId.of("UTC");

        given(clock.instant()).willReturn(now);
        given(focusStatisticsTimeZoneResolver.resolve(userId)).willReturn(userZone);

        FocusTimeOfDayStatisticsResponse response = queryFocusTimeOfDayStatisticsService.query(targetDate);

        assertThat(response.status()).isEqualTo(FocusStatisticsStatus.FUTURE_EMPTY);
        assertThat(response.dataSource()).isEqualTo(FocusStatisticsDataSource.NONE);
        then(focusStatisticsReadPort).should(never()).findSummary(eq(userId), eq(targetDate));
        then(focusStatisticsReadPort).should(never()).findSessions(eq(userId), eq(focusStatisticsPeriodResolver.resolveDay(targetDate, userZone)));
    }

    @Test
    @DisplayName("query buckets clamp cross-day sessions to the target date")
    void query_today_clampsCrossDaySessionsToTargetDateBuckets() {
        givenCurrentUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 12);
        Instant now = ZonedDateTime.of(2026, 4, 12, 0, 30, 0, 0, zoneId).toInstant();
        FocusStatisticsPeriod dayPeriod = focusStatisticsPeriodResolver.resolveDay(targetDate, zoneId);

        FocusStatisticsSessionView crossDaySession = focusSession(
                ZonedDateTime.of(2026, 4, 11, 23, 50, 0, 0, zoneId).toInstant(),
                ZonedDateTime.of(2026, 4, 12, 0, 10, 0, 0, zoneId).toInstant()
        );

        given(clock.instant()).willReturn(now);
        given(focusStatisticsReadPort.findSessions(userId, dayPeriod))
                .willReturn(List.of(crossDaySession));

        FocusTimeOfDayStatisticsResponse response = queryFocusTimeOfDayStatisticsService.query(targetDate);

        assertThat(response.totalFocusSeconds()).isEqualTo(600L);
        assertThat(response.hourlyBuckets().get(0).focusSeconds()).isEqualTo(600L);
        assertThat(response.hourlyBuckets().stream()
                .filter(bucket -> bucket.hourOfDay() != 0)
                .allMatch(bucket -> bucket.focusSeconds() == 0L)).isTrue();
    }

    private FocusStatisticsSummaryView summary(LocalDate date) {
        return new FocusStatisticsSummaryView(
                date,
                8700L,
                List.of(
                        new FocusStatisticsSummaryItemView(
                                ZonedDateTime.of(2026, 4, 10, 0, 20, 0, 0, zoneId).toInstant(),
                                ZonedDateTime.of(2026, 4, 10, 1, 10, 0, 0, zoneId).toInstant()
                        ),
                        new FocusStatisticsSummaryItemView(
                                ZonedDateTime.of(2026, 4, 10, 1, 40, 0, 0, zoneId).toInstant(),
                                ZonedDateTime.of(2026, 4, 10, 3, 15, 0, 0, zoneId).toInstant()
                        )
                )
        );
    }

    private FocusStatisticsSessionView focusSession(Instant startedAt, Instant endedAt) {
        return new FocusStatisticsSessionView(startedAt, endedAt);
    }
}
