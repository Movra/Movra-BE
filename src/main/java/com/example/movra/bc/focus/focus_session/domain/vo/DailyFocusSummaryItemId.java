package com.example.movra.bc.focus.focus_session.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DailyFocusSummaryItemId(
        UUID id
) implements Serializable {

    public static DailyFocusSummaryItemId newId() {
        return new DailyFocusSummaryItemId(UUID.randomUUID());
    }

    public static DailyFocusSummaryItemId of(UUID id) {
        return new DailyFocusSummaryItemId(id);
    }
}
