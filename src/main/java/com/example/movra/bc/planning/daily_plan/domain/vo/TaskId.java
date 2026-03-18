package com.example.movra.bc.planning.daily_plan.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TaskId(
        UUID id
) {

    public static TaskId newId() {
        return new TaskId(UUID.randomUUID());
    }

    public static TaskId of(UUID taskId) {
        return new TaskId(taskId);
    }
}
