package com.example.movra.bc.planning.timetable.domain.event;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.LocalDate;

public record DailyTimetableSummarizedEvent(
        UserId userId,
        LocalDate date,
        int totalCount,
        int completedCount
) {
}
