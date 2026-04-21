package com.example.movra.bc.statistics.focus_statistics.application.service.support.dto;

import java.time.Instant;

public record FocusStatisticsSessionView(
        Instant startedAt,
        Instant endedAt
) {

    public FocusStatisticsSessionView {
        if (startedAt == null) {
            throw new IllegalArgumentException("startedAt must not be null");
        }

        if (endedAt != null && endedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("endedAt must not be before startedAt");
        }
    }

    public boolean isInProgress() {
        return endedAt == null;
    }
}
