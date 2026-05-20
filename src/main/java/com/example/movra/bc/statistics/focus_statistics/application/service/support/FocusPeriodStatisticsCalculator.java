package com.example.movra.bc.statistics.focus_statistics.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsDataSource;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsStatus;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusPeriodStatisticsResult;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSessionView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FocusPeriodStatisticsCalculator {

    private final FocusStatisticsReadPort focusStatisticsReadPort;
    private final FocusStatisticsPeriodResolver focusStatisticsPeriodResolver;
    private final FocusSessionOverlapCalculator focusSessionOverlapCalculator;

    public FocusPeriodStatisticsResult calculate(
            UserId userId,
            FocusStatisticsPeriod period,
            Instant now,
            ZoneId zoneId
    ) {
        LocalDate today = now.atZone(zoneId).toLocalDate();
        List<FocusStatisticsSummaryView> closedSummaries = readClosedSummaries(userId, period, today);
        List<FocusStatisticsPeriod> rawPeriods = resolveRawPeriods(period, today, zoneId, closedSummaries);
        int coveredDayCount = resolveCoveredDayCount(closedSummaries, rawPeriods);

        List<FocusStatisticsSessionView> rawSessions = fetchRawSessions(userId, rawPeriods);
        long totalFocusSeconds = calculateSummarySeconds(closedSummaries)
                + calculateRawSeconds(rawSessions, rawPeriods, now);

        return new FocusPeriodStatisticsResult(
                period.startDate(),
                period.endDate(),
                period.dayCount(),
                coveredDayCount,
                totalFocusSeconds,
                resolveStatus(period, today),
                resolveDataSource(period, today, closedSummaries, rawPeriods)
        );
    }

    private List<FocusStatisticsSummaryView> readClosedSummaries(
            UserId userId,
            FocusStatisticsPeriod period,
            LocalDate today
    ) {
        LocalDate closedEndDate = resolveClosedEndDate(period, today);
        if (closedEndDate == null) {
            return List.of();
        }

        return focusStatisticsReadPort.findSummaryRange(userId, period.startDate(), closedEndDate);
    }

    private List<FocusStatisticsPeriod> resolveRawPeriods(
            FocusStatisticsPeriod period,
            LocalDate today,
            ZoneId zoneId,
            List<FocusStatisticsSummaryView> closedSummaries
    ) {
        List<FocusStatisticsPeriod> rawPeriods = new ArrayList<>(
                resolveMissingClosedRawPeriods(period, today, zoneId, closedSummaries)
        );

        if (includesDate(period, today)) {
            rawPeriods.add(focusStatisticsPeriodResolver.resolveDay(today, zoneId));
        }

        return rawPeriods;
    }

    private List<FocusStatisticsPeriod> resolveMissingClosedRawPeriods(
            FocusStatisticsPeriod period,
            LocalDate today,
            ZoneId zoneId,
            List<FocusStatisticsSummaryView> closedSummaries
    ) {
        LocalDate closedEndDate = resolveClosedEndDate(period, today);
        if (closedEndDate == null) {
            return List.of();
        }

        Set<LocalDate> summarizedDates = closedSummaries.stream()
                .map(FocusStatisticsSummaryView::date)
                .collect(java.util.stream.Collectors.toSet());

        return collapseMissingDatesToPeriods(period.startDate(), closedEndDate, zoneId, summarizedDates);
    }

    private List<FocusStatisticsPeriod> collapseMissingDatesToPeriods(
            LocalDate startDate,
            LocalDate endDate,
            ZoneId zoneId,
            Set<LocalDate> summarizedDates
    ) {
        List<FocusStatisticsPeriod> rawPeriods = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (summarizedDates.contains(currentDate)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            LocalDate rangeStartDate = currentDate;
            while (!currentDate.isAfter(endDate) && !summarizedDates.contains(currentDate)) {
                currentDate = currentDate.plusDays(1);
            }

            LocalDate rangeEndDate = currentDate.minusDays(1);
            rawPeriods.add(focusStatisticsPeriodResolver.resolveRange(rangeStartDate, rangeEndDate, zoneId));
        }

        return rawPeriods;
    }

    private long calculateSummarySeconds(List<FocusStatisticsSummaryView> closedSummaries) {
        return closedSummaries.stream()
                .mapToLong(FocusStatisticsSummaryView::totalSeconds)
                .sum();
    }

    private int resolveCoveredDayCount(
            List<FocusStatisticsSummaryView> closedSummaries,
            List<FocusStatisticsPeriod> rawPeriods
    ) {
        return closedSummaries.size()
                + rawPeriods.stream()
                .mapToInt(FocusStatisticsPeriod::dayCount)
                .sum();
    }

    // rawPeriods 전체를 커버하는 단일 범위 쿼리로 세션을 가져온다.
    // 기존에는 rawPeriod 수 N만큼 개별 쿼리가 발생했고 (monthly 최대 ~16회),
    // 이를 DB 왕복 1회 + 메모리 분배로 대체한다.
    private List<FocusStatisticsSessionView> fetchRawSessions(
            UserId userId,
            List<FocusStatisticsPeriod> rawPeriods
    ) {
        if (rawPeriods.isEmpty()) {
            return List.of();
        }
        Instant globalStart = rawPeriods.stream()
                .map(FocusStatisticsPeriod::startInstant)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        Instant globalEnd = rawPeriods.stream()
                .map(FocusStatisticsPeriod::endInstant)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        return focusStatisticsReadPort.findSessionsInRange(userId, globalStart, globalEnd);
    }

    private long calculateRawSeconds(
            List<FocusStatisticsSessionView> rawSessions,
            List<FocusStatisticsPeriod> rawPeriods,
            Instant now
    ) {
        return rawPeriods.stream()
                .mapToLong(rawPeriod -> calculateRawPeriodSeconds(rawSessions, rawPeriod, now))
                .sum();
    }

    private long calculateRawPeriodSeconds(
            List<FocusStatisticsSessionView> rawSessions,
            FocusStatisticsPeriod rawPeriod,
            Instant now
    ) {
        return rawSessions.stream()
                .filter(s -> s.startedAt().isBefore(rawPeriod.endInstant())
                        && (s.endedAt() == null || s.endedAt().isAfter(rawPeriod.startInstant())))
                .mapToLong(s -> focusSessionOverlapCalculator.overlapSeconds(
                        s, rawPeriod.startInstant(), rawPeriod.endInstant(), now))
                .sum();
    }

    private boolean includesDate(FocusStatisticsPeriod period, LocalDate date) {
        return !date.isBefore(period.startDate()) && !date.isAfter(period.endDate());
    }

    private FocusStatisticsStatus resolveStatus(FocusStatisticsPeriod period, LocalDate today) {
        if (period.startDate().isAfter(today)) {
            return FocusStatisticsStatus.FUTURE_EMPTY;
        }

        return includesDate(period, today)
                ? FocusStatisticsStatus.PARTIAL
                : FocusStatisticsStatus.FINAL;
    }

    private FocusStatisticsDataSource resolveDataSource(
            FocusStatisticsPeriod period,
            LocalDate today,
            List<FocusStatisticsSummaryView> closedSummaries,
            List<FocusStatisticsPeriod> rawPeriods
    ) {
        if (resolveStatus(period, today) == FocusStatisticsStatus.FUTURE_EMPTY) {
            return FocusStatisticsDataSource.NONE;
        }

        boolean usesSummary = !closedSummaries.isEmpty();
        boolean usesRaw = !rawPeriods.isEmpty();

        if (usesSummary && usesRaw) {
            return FocusStatisticsDataSource.MIXED;
        }

        if (usesSummary) {
            return FocusStatisticsDataSource.SUMMARY;
        }

        return FocusStatisticsDataSource.RAW;
    }

    private LocalDate resolveClosedEndDate(FocusStatisticsPeriod period, LocalDate today) {
        LocalDate closedEndDate = period.endDate().isBefore(today) ? period.endDate() : today.minusDays(1);
        return closedEndDate.isBefore(period.startDate()) ? null : closedEndDate;
    }
}
