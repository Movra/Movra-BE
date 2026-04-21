package com.example.movra.bc.statistics.focus_statistics.application.service.support.dto;

import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsDataSource;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusStatisticsStatus;

import java.time.LocalDate;

public record FocusPeriodStatisticsResult(
        LocalDate periodStartDate,
        LocalDate periodEndDate,
        int dayCount,
        int coveredDayCount,
        long totalFocusSeconds,
        FocusStatisticsStatus status,
        FocusStatisticsDataSource dataSource
) {

    public FocusPeriodStatisticsResult {
        if (periodStartDate == null || periodEndDate == null) {
            throw new IllegalArgumentException("period dates must not be null");
        }

        if (periodEndDate.isBefore(periodStartDate)) {
            throw new IllegalArgumentException("periodEndDate must not be before periodStartDate");
        }

        if (dayCount < 1) {
            throw new IllegalArgumentException("dayCount must be >= 1");
        }

        if (coveredDayCount < 0 || coveredDayCount > dayCount) {
            throw new IllegalArgumentException("coveredDayCount must be between 0 and dayCount");
        }

        if (totalFocusSeconds < 0) {
            throw new IllegalArgumentException("totalFocusSeconds must be >= 0");
        }

        if (coveredDayCount == 0 && totalFocusSeconds > 0) {
            throw new IllegalArgumentException("coveredDayCount must be > 0 when totalFocusSeconds is positive");
        }

        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource must not be null");
        }
    }

    public long averageDailyFocusSeconds() {
        if (coveredDayCount == 0) {
            return 0L;
        }

        return totalFocusSeconds / coveredDayCount;
    }
}
