package com.example.movra.bc.planning.exam_schedule.application.service.dto.response;

import com.example.movra.bc.planning.exam_schedule.domain.type.SeasonMode;
import lombok.Builder;

@Builder
public record SeasonModeResponse(
        SeasonMode seasonMode,
        ExamScheduleResponse nextExamSchedule
) {

    public static SeasonModeResponse baseline() {
        return SeasonModeResponse.builder()
                .seasonMode(SeasonMode.BASELINE_MODE)
                .nextExamSchedule(null)
                .build();
    }

    public static SeasonModeResponse from(ExamScheduleResponse nextExamSchedule) {
        if (nextExamSchedule == null) {
            return baseline();
        }

        return SeasonModeResponse.builder()
                .seasonMode(nextExamSchedule.seasonMode())
                .nextExamSchedule(nextExamSchedule)
                .build();
    }
}
