package com.example.movra.bc.insight.behavior_insight.application.service.dto.response;

import com.example.movra.bc.insight.behavior_insight.domain.InsightReport;
import com.example.movra.bc.insight.behavior_insight.domain.type.InsightReportStatus;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightMetrics;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InsightReportResponse(
        UUID insightReportId,
        LocalDate periodStart,
        LocalDate periodEnd,
        InsightReportStatus status,
        Instant generatedAt,
        InsightMetrics metrics,
        InsightNarrativeResponse narrative
) {

    public static InsightReportResponse from(InsightReport report) {
        return new InsightReportResponse(
                report.getId().id(),
                report.getPeriod().periodStart(),
                report.getPeriod().periodEnd(),
                report.getStatus(),
                report.getGeneratedAt(),
                report.getMetrics(),
                InsightNarrativeResponse.from(report.getNarrative())
        );
    }
}
