package com.example.movra.bc.accountability.accountability_relation.application.service.dto.response;

import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryView;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryView;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryView;

import java.time.LocalDate;

public record WatcherOverviewResponse(
        FriendAccountabilityRelationResponse relation,
        LocalDate date,
        DailyFocusSummaryView focusSessions,
        DailyTopPicksSummaryView topPicks,
        DailyTimetableSummaryView timetableTasks
) {
}
