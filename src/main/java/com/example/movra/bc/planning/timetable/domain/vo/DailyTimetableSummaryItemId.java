package com.example.movra.bc.planning.timetable.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DailyTimetableSummaryItemId(
        UUID id
) implements Serializable {

    public static DailyTimetableSummaryItemId newId() {
        return new DailyTimetableSummaryItemId(UUID.randomUUID());
    }

    public static DailyTimetableSummaryItemId of(UUID id) {
        return new DailyTimetableSummaryItemId(id);
    }
}
