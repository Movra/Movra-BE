package com.example.movra.bc.feedback.daily_reflection.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DailyReflectionId(
        UUID id
) implements Serializable {

    public static DailyReflectionId newId() {
        return new DailyReflectionId(UUID.randomUUID());
    }

    public static DailyReflectionId of(UUID id) {
        return new DailyReflectionId(id);
    }
}
