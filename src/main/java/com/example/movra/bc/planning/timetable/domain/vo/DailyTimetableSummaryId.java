package com.example.movra.bc.planning.timetable.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record DailyTimetableSummaryId(
        UUID id
) implements Serializable {

    public static DailyTimetableSummaryId newId() {
        return new DailyTimetableSummaryId(UUID.randomUUID());
    }

    public static DailyTimetableSummaryId of(UUID id) {
        return new DailyTimetableSummaryId(id);
    }
}
