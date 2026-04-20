package com.example.movra.bc.focus.focus_session.application.service.support.dto;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.LocalDate;
import java.util.List;

public record DailyFocusSummaryView(
        UserId userId,
        LocalDate date,
        long totalSeconds,
        int sessionCount,
        List<DailyFocusSummaryItemView> items
) {
}
