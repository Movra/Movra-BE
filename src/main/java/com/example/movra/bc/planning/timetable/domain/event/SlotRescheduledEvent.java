package com.example.movra.bc.planning.timetable.domain.event;

import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;

public record SlotRescheduledEvent(
        DailyPlanId dailyPlanId,
        TaskId taskId,
        int newEstimatedMinutes
) {}
