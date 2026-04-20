package com.example.movra.bc.planning.timetable.application.service.support.dto;

import java.time.LocalTime;

public record DailyTimetableSummaryItemView(
        String contentSnapshot,
        boolean completedSnapshot,
        LocalTime startTimeSnapshot,
        LocalTime endTimeSnapshot,
        boolean topPickSnapshot,
        int displayOrder
) {
}
