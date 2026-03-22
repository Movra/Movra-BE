package com.example.movra.bc.visioning.future_vision.application.service.dto.response;

import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record YearlyVisionResponse(
        UUID futureVisionId,
        String yearlyVisionImageUrl,
        String yearlyVisionDescription,
        LocalDate yearlyVisionCreatedAt
) {

    public static YearlyVisionResponse from(FutureVision futureVision) {
        return YearlyVisionResponse.builder()
                .futureVisionId(futureVision.getId().id())
                .yearlyVisionImageUrl(futureVision.getYearlyVisionImageUrl())
                .yearlyVisionDescription(futureVision.getYearlyVisionDescription())
                .yearlyVisionCreatedAt(futureVision.getYearlyVisionCreatedAt())
                .build();
    }
}
