package com.example.movra.bc.planning.daily_plan.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DailyTopPicksSummaryId(
        UUID id
) implements Serializable {

    public static DailyTopPicksSummaryId newId() {
        return new DailyTopPicksSummaryId(UUID.randomUUID());
    }

    public static DailyTopPicksSummaryId of(UUID id) {
        return new DailyTopPicksSummaryId(id);
    }
}
