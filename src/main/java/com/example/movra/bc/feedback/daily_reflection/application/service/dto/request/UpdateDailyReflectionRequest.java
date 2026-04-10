package com.example.movra.bc.feedback.daily_reflection.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDailyReflectionRequest(
        @NotBlank
        @Size(max = 500)
        String whatWentWell,

        @NotBlank
        @Size(max = 1000)
        String whatBrokeDown,

        @NotBlank
        @Size(max = 500)
        String nextAction
) {
}
