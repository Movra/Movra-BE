package com.example.movra.bc.focus.focus_session.application.service.support.dto;

import java.time.Instant;

public record DailyFocusSummaryItemView(
        Instant startedAtSnapshot,
        Instant endedAtSnapshot,
        Long recordedDurationSecondsSnapshot,
        Instant overlapStartedAt,
        Instant overlapEndedAt,
        long overlapSeconds,
        int displayOrder
) {
}
