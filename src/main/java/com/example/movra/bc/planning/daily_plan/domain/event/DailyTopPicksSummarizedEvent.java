package com.example.movra.bc.planning.daily_plan.domain.event;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.LocalDate;

public record DailyTopPicksSummarizedEvent(
        UserId userId,
        LocalDate date,
        int totalCount,
        int completedCount
) {
}
