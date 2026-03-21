package com.example.movra.bc.visioning.future_vision.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWeeklyVisionRequest(

        @NotBlank(message = "weeklyVisionImageUrl is required.")
        @Size(max = 255, message = "weeklyVisionImageUrl must be 255 characters or fewer.")
        String weeklyVisionImageUrl
) {
}
