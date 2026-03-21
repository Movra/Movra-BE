package com.example.movra.bc.visioning.future_vision.application.service.dto.response;

import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import lombok.Builder;

import java.util.UUID;

@Builder
public record WeeklyVisionResponse(
        UUID futureVisionId,
        String weeklyVisionImageUrl
) {

    public static WeeklyVisionResponse from(FutureVision futureVision) {
        return WeeklyVisionResponse.builder()
                .futureVisionId(futureVision.getId().id())
                .weeklyVisionImageUrl(futureVision.getWeeklyVisionImageUrl())
                .build();
    }
}
