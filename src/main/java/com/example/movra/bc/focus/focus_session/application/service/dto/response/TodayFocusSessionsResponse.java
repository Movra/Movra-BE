package com.example.movra.bc.focus.focus_session.application.service.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
public record TodayFocusSessionsResponse(
        LocalDate targetDate,
        Instant queriedAt,
        long totalFocusSeconds,
        boolean focusing,
        List<FocusSessionResponse> sessions
) {
}
