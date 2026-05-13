package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response;

import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.response.TopPicksResponse;
import com.example.movra.bc.planning.timetable.application.service.dto.response.TimetableResponse;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record TodayPlanningOverviewResponse(
        UUID dailyPlanId,
        LocalDate targetDate,
        List<TopPicksResponse> topPicks,
        TimetableResponse timetable
) {
}
