package com.example.movra.bc.statistics.focus_statistics.application.service.support.dto;

import java.time.Instant;

public record FocusStatisticsSummaryItemView(
        Instant overlapStartedAt,
        Instant overlapEndedAt
) {

    public FocusStatisticsSummaryItemView {
        if (overlapStartedAt == null || overlapEndedAt == null) {
            throw new IllegalArgumentException("overlap range must not be null");
        }

        if (!overlapEndedAt.isAfter(overlapStartedAt)) {
            throw new IllegalArgumentException("overlapEndedAt must be after overlapStartedAt");
        }
    }
}
