package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.request;

import java.time.LocalDate;

public record DailyPlanRequest(
        LocalDate planDate
) {
}
