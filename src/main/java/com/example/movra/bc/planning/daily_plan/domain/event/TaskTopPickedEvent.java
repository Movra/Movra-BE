package com.example.movra.bc.planning.daily_plan.domain.event;

import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;

public record TaskTopPickedEvent(
        DailyPlanId dailyPlanId,
        TaskId taskId
) {}
