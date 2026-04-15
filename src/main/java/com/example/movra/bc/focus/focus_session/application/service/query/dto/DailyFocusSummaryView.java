package com.example.movra.bc.focus.focus_session.application.service.query.dto;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.LocalDate;

public record DailyFocusSummaryView(
        UserId userId,
        LocalDate date,
        long totalSeconds,
        int sessionCount
) {
}
