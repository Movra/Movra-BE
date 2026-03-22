package com.example.movra.bc.visioning.future_vision.application.service.dto.response;

import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record FutureVisionResponse(
        UUID futureVisionId,
        String weeklyVisionImageUrl,
        String yearlyVisionImageUrl,
        String yearlyVisionDescription,
        LocalDate yearlyVisionCreatedAt
) {

    public static FutureVisionResponse from(FutureVision futureVision) {
        return FutureVisionResponse.builder()
                .futureVisionId(futureVision.getId().id())
                .weeklyVisionImageUrl(futureVision.getWeeklyVisionImageUrl())
                .yearlyVisionImageUrl(futureVision.getYearlyVisionImageUrl())
                .yearlyVisionDescription(futureVision.getYearlyVisionDescription())
                .yearlyVisionCreatedAt(futureVision.getYearlyVisionCreatedAt())
                .build();
    }
}
