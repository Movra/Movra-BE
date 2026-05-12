package com.example.movra.bc.planning.exam_schedule.application.service.dto.request;

import com.example.movra.bc.planning.exam_schedule.domain.type.ExamType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ExamScheduleRequest(
        @NotNull
        ExamType examType,

        @NotBlank
        @Size(max = 100)
        String title,

        @NotNull
        LocalDate examDate,

        @Size(max = 50)
        String subject
) {
}
