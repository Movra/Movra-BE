package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response;

import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record DailyPlanResponse(
        UUID dailyPlanId,
        LocalDate planDate,
        List<TaskResponse> tasks
) {

    public static DailyPlanResponse from(DailyPlan dailyPlan) {
        return DailyPlanResponse.builder()
                .dailyPlanId(dailyPlan.getDailyPlanId().id())
                .planDate(dailyPlan.getPlanDate())
                .tasks(dailyPlan.getTasks().stream()
                        .map(TaskResponse::from)
                        .toList())
                .build();
    }
}
