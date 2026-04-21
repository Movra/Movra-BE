package com.example.movra.bc.statistics.focus_statistics.application.service.support;

import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSessionView;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class FocusSessionOverlapCalculator {

    public long overlapSeconds(FocusStatisticsSessionView focusSession, Instant periodStart, Instant periodEnd, Instant now) {
        Instant effectiveEnd = focusSession.isInProgress() ? now : focusSession.endedAt();
        Instant overlapStart = focusSession.startedAt().isAfter(periodStart)
                ? focusSession.startedAt()
                : periodStart;
        Instant overlapEnd = effectiveEnd.isBefore(periodEnd)
                ? effectiveEnd
                : periodEnd;

        if (!overlapEnd.isAfter(overlapStart)) {
            return 0L;
        }

        return Duration.between(overlapStart, overlapEnd).getSeconds();
    }
}
