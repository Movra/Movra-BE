package com.example.movra.bc.insight.behavior_insight.domain.type;

/**
 * 리포트 생성 단계.
 * - METRICS_READY: 결정론적 지표 계산·영속화 완료(서사 미생성).
 * - COMPLETED: LLM 서사까지 생성 완료.
 * - NARRATIVE_FAILED: 지표는 있으나 서사 생성 실패(재시도 대상).
 */
public enum InsightReportStatus {
    METRICS_READY,
    COMPLETED,
    NARRATIVE_FAILED
}
