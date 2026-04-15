package com.example.movra.bc.planning.daily_plan.application.service.query.dto;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.LocalDate;

public record DailyTopPicksSummaryView(
        UserId userId,
        LocalDate date,
        int totalCount,
        int completedCount
) {
}
