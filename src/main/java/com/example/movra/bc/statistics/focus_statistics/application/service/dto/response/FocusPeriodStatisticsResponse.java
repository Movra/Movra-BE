package com.example.movra.bc.statistics.focus_statistics.application.service.dto.response;

import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusPeriodStatisticsResult;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;

@Builder
public record FocusPeriodStatisticsResponse(
        LocalDate targetDate,
        Instant queriedAt,
        LocalDate periodStartDate,
        LocalDate periodEndDate,
        int dayCount,
        int coveredDayCount,
        long totalFocusSeconds,
        long averageDailyFocusSeconds,
        FocusStatisticsStatus status,
        FocusStatisticsDataSource dataSource
) {

    public static FocusPeriodStatisticsResponse from(
            LocalDate targetDate,
            Instant queriedAt,
            FocusPeriodStatisticsResult result
    ) {
        return FocusPeriodStatisticsResponse.builder()
                .targetDate(targetDate)
                .queriedAt(queriedAt)
                .periodStartDate(result.periodStartDate())
                .periodEndDate(result.periodEndDate())
                .dayCount(result.dayCount())
                .coveredDayCount(result.coveredDayCount())
                .totalFocusSeconds(result.totalFocusSeconds())
                .averageDailyFocusSeconds(result.averageDailyFocusSeconds())
                .status(result.status())
                .dataSource(result.dataSource())
                .build();
    }
}
