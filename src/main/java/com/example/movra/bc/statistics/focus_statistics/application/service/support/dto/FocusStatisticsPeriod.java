package com.example.movra.bc.statistics.focus_statistics.application.service.support.dto;

import java.time.Instant;
import java.time.LocalDate;

public record FocusStatisticsPeriod(
        LocalDate startDate,
        LocalDate endDate,
        Instant startInstant,
        Instant endInstant,
        int dayCount
) {

    public FocusStatisticsPeriod {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }

        if (!endInstant.isAfter(startInstant)) {
            throw new IllegalArgumentException("endInstant must be after startInstant");
        }

        if (dayCount < 1) {
            throw new IllegalArgumentException("dayCount must be >= 1");
        }
    }
}
