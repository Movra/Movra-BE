package com.example.movra.bc.planning.exam_schedule.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ExamScheduleId(UUID id) {

    public static ExamScheduleId newId() {
        return new ExamScheduleId(UUID.randomUUID());
    }

    public static ExamScheduleId of(UUID id) {
        return new ExamScheduleId(id);
    }
}
