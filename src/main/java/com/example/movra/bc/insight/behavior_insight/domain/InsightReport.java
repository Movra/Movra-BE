package com.example.movra.bc.insight.behavior_insight.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.domain.event.InsightReportGeneratedEvent;
import com.example.movra.bc.insight.behavior_insight.domain.type.InsightReportStatus;
import com.example.movra.bc.insight.behavior_insight.domain.vo.AnalysisPeriod;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightMetrics;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightNarrative;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightReportId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 사용자×기간 단위 행동 분석 리포트(애그리거트 루트).
 * Phase 1은 결정론적 지표(metrics)까지 채워 METRICS_READY 상태로 생성한다.
 * LLM 서사(narrative)는 Phase 2에서 추가한다.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_insight_report", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "period_start"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InsightReport extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "insight_report_id"))
    private InsightReportId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Embedded
    private AnalysisPeriod period;

    @Embedded
    private InsightMetrics metrics;

    @Embedded
    private InsightNarrative narrative;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private InsightReportStatus status;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    /**
     * 결정론적 지표만 채워 리포트를 생성한다(METRICS_READY).
     */
    public static InsightReport ofMetrics(
            UserId userId,
            AnalysisPeriod period,
            InsightMetrics metrics,
            Instant generatedAt
    ) {
        InsightReport report = InsightReport.builder()
                .id(InsightReportId.newId())
                .userId(userId)
                .period(period)
                .metrics(metrics)
                .status(InsightReportStatus.METRICS_READY)
                .generatedAt(generatedAt)
                .build();

        report.registerEvent(new InsightReportGeneratedEvent(report.id, userId));
        return report;
    }

    /**
     * LLM 서사 생성 성공: 서사를 채우고 COMPLETED로 전이.
     */
    public void markNarrativeCompleted(InsightNarrative narrative) {
        this.narrative = narrative;
        this.status = InsightReportStatus.COMPLETED;
    }

    /**
     * LLM 서사 생성 실패: 지표는 보존하고 NARRATIVE_FAILED로 전이(재시도 대상).
     */
    public void markNarrativeFailed() {
        this.status = InsightReportStatus.NARRATIVE_FAILED;
    }
}
