package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.LocalDate;
import java.util.List;

public record DailyTopPicksSummaryView(
        UserId userId,
        LocalDate date,
        int totalCount,
        int completedCount,
        List<DailyTopPicksSummaryItemView> items
) {
}
