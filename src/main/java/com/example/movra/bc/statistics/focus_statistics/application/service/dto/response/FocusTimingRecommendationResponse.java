package com.example.movra.bc.statistics.focus_statistics.application.service.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
public record FocusTimingRecommendationResponse(
        LocalDate targetDate,
        Instant queriedAt,
        List<RecommendedHour> recommendedHours,
        String reason,
        boolean basedOnData
) {

    public record RecommendedHour(
            int hourOfDay,
            long averageFocusSeconds
    ) {}
}
