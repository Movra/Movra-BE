package com.example.movra.bc.statistics.focus_statistics.application.service.support.dto;

import java.time.LocalDate;
import java.util.List;

public record FocusStatisticsSummaryView(
        LocalDate date,
        long totalSeconds,
        List<FocusStatisticsSummaryItemView> items
) {

    public FocusStatisticsSummaryView {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }

        if (totalSeconds < 0) {
            throw new IllegalArgumentException("totalSeconds must be >= 0");
        }

        items = items == null ? List.of() : List.copyOf(items);
    }
}
