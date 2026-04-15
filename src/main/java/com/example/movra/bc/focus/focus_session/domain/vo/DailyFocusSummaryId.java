package com.example.movra.bc.focus.focus_session.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DailyFocusSummaryId(
        UUID id
) implements Serializable {

    public static DailyFocusSummaryId newId() {
        return new DailyFocusSummaryId(UUID.randomUUID());
    }

    public static DailyFocusSummaryId of(UUID id) {
        return new DailyFocusSummaryId(id);
    }
}
