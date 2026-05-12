package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DailyPlanRequest(
        @NotNull
        LocalDate planDate
) {
}
