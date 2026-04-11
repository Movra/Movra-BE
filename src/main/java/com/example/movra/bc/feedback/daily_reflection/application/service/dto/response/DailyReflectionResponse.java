package com.example.movra.bc.feedback.daily_reflection.application.service.dto.response;

import com.example.movra.bc.feedback.daily_reflection.domain.DailyReflection;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record DailyReflectionResponse(
        UUID dailyReflectionId,
        LocalDate reflectionDate,
        String whatWentWell,
        String whatBrokeDown,
        String nextAction
) {

    public static DailyReflectionResponse from(DailyReflection dailyReflection) {
        return DailyReflectionResponse.builder()
                .dailyReflectionId(dailyReflection.getId().id())
                .reflectionDate(dailyReflection.getReflectionDate())
                .whatWentWell(dailyReflection.getWhatWentWell())
                .whatBrokeDown(dailyReflection.getWhatBrokeDown())
                .nextAction(dailyReflection.getNextAction())
                .build();
    }
}
