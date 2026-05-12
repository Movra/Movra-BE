package com.example.movra.bc.planning.exam_schedule.application.service.dto.response;

import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.type.ExamType;
import com.example.movra.bc.planning.exam_schedule.domain.type.SeasonMode;
import lombok.Builder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Builder
public record ExamScheduleResponse(
        UUID examScheduleId,
        ExamType examType,
        String title,
        LocalDate examDate,
        String subject,
        long daysUntil,
        SeasonMode seasonMode
) {

    public static ExamScheduleResponse from(ExamSchedule examSchedule, LocalDate today) {
        long daysUntil = ChronoUnit.DAYS.between(today, examSchedule.getExamDate());

        return ExamScheduleResponse.builder()
                .examScheduleId(examSchedule.getExamScheduleId().id())
                .examType(examSchedule.getExamType())
                .title(examSchedule.getTitle())
                .examDate(examSchedule.getExamDate())
                .subject(examSchedule.getSubject())
                .daysUntil(daysUntil)
                .seasonMode(SeasonMode.determine(examSchedule.getExamType(), daysUntil))
                .build();
    }
}
