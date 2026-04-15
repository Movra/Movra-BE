package com.example.movra.bc.planning.timetable.application.service.query.dto;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.LocalDate;

public record DailyTimetableSummaryView(
        UserId userId,
        LocalDate date,
        int totalCount,
        int completedCount
) {
}
