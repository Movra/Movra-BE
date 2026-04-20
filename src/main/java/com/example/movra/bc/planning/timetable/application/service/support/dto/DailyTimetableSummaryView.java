package com.example.movra.bc.planning.timetable.application.service.support.dto;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.LocalDate;
import java.util.List;

public record DailyTimetableSummaryView(
        UserId userId,
        LocalDate date,
        int totalCount,
        int completedCount,
        List<DailyTimetableSummaryItemView> items
) {
}
