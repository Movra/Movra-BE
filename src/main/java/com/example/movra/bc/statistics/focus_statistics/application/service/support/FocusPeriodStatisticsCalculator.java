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

        long totalFocusSeconds = calculateSummarySeconds(closedSummaries)
                + calculateRawSeconds(userId, rawPeriods, now);

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

    private long calculateRawSeconds(
            UserId userId,
            List<FocusStatisticsPeriod> rawPeriods,
            Instant now
    ) {
        return rawPeriods.stream()
                .mapToLong(rawPeriod -> calculateRawPeriodSeconds(userId, rawPeriod, now))
                .sum();
    }

    private long calculateRawPeriodSeconds(
            UserId userId,
            FocusStatisticsPeriod rawPeriod,
            Instant now
    ) {
        List<FocusStatisticsSessionView> focusSessions = focusStatisticsReadPort.findSessions(userId, rawPeriod);

        return focusSessions.stream()
                .mapToLong(focusSession -> focusSessionOverlapCalculator.overlapSeconds(
                        focusSession,
                        rawPeriod.startInstant(),
                        rawPeriod.endInstant(),
                        now
                ))
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
