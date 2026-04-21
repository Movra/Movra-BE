package com.example.movra.bc.statistics.focus_statistics.application.service.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
public record FocusTimeOfDayStatisticsResponse(
        LocalDate targetDate,
        Instant queriedAt,
        long totalFocusSeconds,
        FocusStatisticsStatus status,
        FocusStatisticsDataSource dataSource,
        List<FocusTimeBucketResponse> hourlyBuckets
) {
}
