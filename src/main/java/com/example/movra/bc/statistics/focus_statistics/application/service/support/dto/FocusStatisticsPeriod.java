package com.example.movra.bc.statistics.focus_statistics.application.service.support.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record FocusStatisticsPeriod(
        LocalDate startDate,
        LocalDate endDate,
        Instant startInstant,
        Instant endInstant,
        int dayCount
) {

    public FocusStatisticsPeriod {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("dates must not be null");
        }

        if (startInstant == null || endInstant == null) {
            throw new IllegalArgumentException("instants must not be null");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }

        if (!endInstant.isAfter(startInstant)) {
            throw new IllegalArgumentException("endInstant must be after startInstant");
        }

        if (dayCount < 1) {
            throw new IllegalArgumentException("dayCount must be >= 1");
        }

        int expectedDayCount = Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)));
        if (dayCount != expectedDayCount) {
            throw new IllegalArgumentException("dayCount must match the inclusive date range");
        }
    }
}
