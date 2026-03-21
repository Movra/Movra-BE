package com.example.movra.bc.visioning.future_vision.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateYearlyVisionRequest(

        @NotBlank(message = "yearlyVisionImageUrl is required.")
        @Size(max = 255, message = "yearlyVisionImageUrl must be 255 characters or fewer.")
        String yearlyVisionImageUrl,

        @NotBlank(message = "yearlyVisionDescription is required.")
        @Size(max = 100, message = "yearlyVisionDescription must be 100 characters or fewer.")
        String yearlyVisionDescription
) {
}
