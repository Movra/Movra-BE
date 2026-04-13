package com.example.movra.bc.focus.focus_session.application.helper;

import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class FocusSessionTimeCalculator {

    public long overlapSeconds(FocusSession focusSession, Instant periodStart, Instant periodEnd, Instant now) {
        Instant effectiveEnd = focusSession.isInProgress() ? now : focusSession.getEndedAt();
        Instant overlapStart = focusSession.getStartedAt().isAfter(periodStart)
                ? focusSession.getStartedAt()
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
