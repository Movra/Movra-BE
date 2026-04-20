package com.example.movra.bc.planning.daily_plan.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record DailyTopPicksSummaryItemId(
        String id
) {
    public static DailyTopPicksSummaryItemId newId() {
        return new DailyTopPicksSummaryItemId(UUID.randomUUID().toString());
    }
}
