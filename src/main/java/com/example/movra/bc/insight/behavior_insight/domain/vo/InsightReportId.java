package com.example.movra.bc.insight.behavior_insight.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record InsightReportId(
        UUID id
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static InsightReportId newId() {
        return new InsightReportId(UUID.randomUUID());
    }

    public static InsightReportId of(UUID id) {
        return new InsightReportId(id);
    }
}
