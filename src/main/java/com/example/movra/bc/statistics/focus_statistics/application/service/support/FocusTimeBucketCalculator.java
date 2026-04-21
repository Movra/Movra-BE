package com.example.movra.bc.statistics.focus_statistics.application.service.support;

import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSessionView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryItemView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusTimeBucketView;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class FocusTimeBucketCalculator {

    private static final int HOURS_PER_DAY = 24;

    public List<FocusTimeBucketView> calculate(
            List<FocusStatisticsSessionView> focusSessions,
            FocusStatisticsPeriod period,
            ZoneId zoneId,
            Instant now
    ) {
        if (period.dayCount() != 1) {
            throw new IllegalArgumentException("time-of-day buckets require a single-day period");
        }

        long[] hourlyFocusSeconds = new long[HOURS_PER_DAY];

        for (FocusStatisticsSessionView focusSession : focusSessions) {
            Instant effectiveEnd = focusSession.isInProgress() ? now : focusSession.endedAt();
            Instant rangeStart = focusSession.startedAt().isAfter(period.startInstant())
                    ? focusSession.startedAt()
                    : period.startInstant();
            Instant rangeEnd = effectiveEnd.isBefore(period.endInstant())
                    ? effectiveEnd
                    : period.endInstant();

            accumulateRange(hourlyFocusSeconds, rangeStart, rangeEnd, zoneId);
        }

        return toViews(hourlyFocusSeconds);
    }

    public List<FocusTimeBucketView> calculate(FocusStatisticsSummaryView summary, ZoneId zoneId) {
        long[] hourlyFocusSeconds = new long[HOURS_PER_DAY];

        for (FocusStatisticsSummaryItemView item : summary.items()) {
            accumulateRange(hourlyFocusSeconds, item.overlapStartedAt(), item.overlapEndedAt(), zoneId);
        }

        return toViews(hourlyFocusSeconds);
    }

    private void accumulateRange(
            long[] hourlyFocusSeconds,
            Instant rangeStart,
            Instant rangeEnd,
            ZoneId zoneId
    ) {
        if (!rangeEnd.isAfter(rangeStart)) {
            return;
        }

        Instant cursorInstant = rangeStart;
        ZonedDateTime cursor = rangeStart.atZone(zoneId);

        while (cursorInstant.isBefore(rangeEnd)) {
            ZonedDateTime nextHour = cursor.withMinute(0).withSecond(0).withNano(0).plusHours(1);
            Instant effectiveEnd = nextHour.toInstant().isBefore(rangeEnd)
                    ? nextHour.toInstant()
                    : rangeEnd;

            hourlyFocusSeconds[cursor.getHour()] += Duration.between(cursorInstant, effectiveEnd).getSeconds();

            cursorInstant = effectiveEnd;
            cursor = cursorInstant.atZone(zoneId);
        }
    }

    private List<FocusTimeBucketView> toViews(long[] hourlyFocusSeconds) {
        return IntStream.range(0, HOURS_PER_DAY)
                .mapToObj(hour -> new FocusTimeBucketView(hour, hourlyFocusSeconds[hour]))
                .toList();
    }
}
