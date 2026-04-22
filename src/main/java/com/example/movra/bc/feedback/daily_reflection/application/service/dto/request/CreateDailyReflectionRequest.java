package com.example.movra.bc.feedback.daily_reflection.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateDailyReflectionRequest(
        @NotNull
        LocalDate reflectionDate,

        @NotBlank
        @Size(max = 500)
        String whatWentWell,

        @NotBlank
        @Size(max = 1000)
        String whatBrokeDown,

        @NotBlank
        @Size(max = 500)
        String ifCondition,

        @NotBlank
        @Size(max = 500)
        String thenAction
) {
}
