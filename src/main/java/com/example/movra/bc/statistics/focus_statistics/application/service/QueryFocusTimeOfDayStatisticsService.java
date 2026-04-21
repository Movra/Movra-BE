package com.example.movra.bc.statistics.focus_statistics.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsDataSource;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsStatus;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimeBucketResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimeOfDayStatisticsResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsPeriodResolver;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsReadPort;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusSessionOverlapCalculator;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusTimeBucketCalculator;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSessionView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusTimeBucketView;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryFocusTimeOfDayStatisticsService {

    private final FocusStatisticsReadPort focusStatisticsReadPort;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;
    private final FocusStatisticsPeriodResolver focusStatisticsPeriodResolver;
    private final FocusTimeBucketCalculator focusTimeBucketCalculator;
    private final FocusSessionOverlapCalculator focusSessionOverlapCalculator;

    @Transactional(readOnly = true)
    public FocusTimeOfDayStatisticsResponse query(LocalDate targetDate) {
        Instant now = clock.instant();
        UserId userId = currentUserQuery.currentUser().userId();
        ZoneId zoneId = clock.getZone();
        LocalDate today = now.atZone(zoneId).toLocalDate();

        if (targetDate.isAfter(today)) {
            return emptyStatistics(targetDate, now);
        }

        if (targetDate.isBefore(today)) {
            return focusStatisticsReadPort.findSummary(userId, targetDate)
                    .map(summary -> buildFromSummary(targetDate, now, zoneId, summary))
                    .orElseGet(() -> buildFromRaw(targetDate, userId, now, zoneId, FocusStatisticsStatus.FINAL));
        }

        return buildFromRaw(targetDate, userId, now, zoneId, FocusStatisticsStatus.PARTIAL);
    }

    private FocusTimeOfDayStatisticsResponse buildFromSummary(
            LocalDate targetDate,
            Instant now,
            ZoneId zoneId,
            FocusStatisticsSummaryView summary
    ) {
        return buildResponse(
                targetDate,
                now,
                summary.totalSeconds(),
                FocusStatisticsStatus.FINAL,
                FocusStatisticsDataSource.SUMMARY,
                focusTimeBucketCalculator.calculate(summary, zoneId)
        );
    }

    private FocusTimeOfDayStatisticsResponse buildFromRaw(
            LocalDate targetDate,
            UserId userId,
            Instant now,
            ZoneId zoneId,
            FocusStatisticsStatus status
    ) {
        FocusStatisticsPeriod period = focusStatisticsPeriodResolver.resolveDay(targetDate, zoneId);
        List<FocusStatisticsSessionView> focusSessions = focusStatisticsReadPort.findSessions(userId, period);

        long totalFocusSeconds = focusSessions.stream()
                .mapToLong(focusSession -> focusSessionOverlapCalculator.overlapSeconds(
                        focusSession,
                        period.startInstant(),
                        period.endInstant(),
                        now
                ))
                .sum();

        return buildResponse(
                targetDate,
                now,
                totalFocusSeconds,
                status,
                FocusStatisticsDataSource.RAW,
                focusTimeBucketCalculator.calculate(focusSessions, period, zoneId, now)
        );
    }

    private FocusTimeOfDayStatisticsResponse buildResponse(
            LocalDate targetDate,
            Instant now,
            long totalFocusSeconds,
            FocusStatisticsStatus status,
            FocusStatisticsDataSource dataSource,
            List<FocusTimeBucketView> hourlyBuckets
    ) {
        return FocusTimeOfDayStatisticsResponse.builder()
                .targetDate(targetDate)
                .queriedAt(now)
                .totalFocusSeconds(totalFocusSeconds)
                .status(status)
                .dataSource(dataSource)
                .hourlyBuckets(hourlyBuckets.stream()
                        .map(bucket -> new FocusTimeBucketResponse(bucket.hourOfDay(), bucket.focusSeconds()))
                        .toList())
                .build();
    }

    private FocusTimeOfDayStatisticsResponse emptyStatistics(LocalDate targetDate, Instant now) {
        return buildResponse(
                targetDate,
                now,
                0L,
                FocusStatisticsStatus.FUTURE_EMPTY,
                FocusStatisticsDataSource.NONE,
                java.util.stream.IntStream.range(0, 24)
                        .mapToObj(hour -> new FocusTimeBucketView(hour, 0L))
                        .toList()
        );
    }
}
