package com.example.movra.bc.insight.behavior_insight.application.service.support.dto;

import java.time.Instant;

/**
 * 집중 세션 한 건의 시작·종료 시각. endedAt이 null이면 진행 중(미집계).
 */
public record FocusSessionView(
        Instant startedAt,
        Instant endedAt
) {
    public boolean isCompleted() {
        return endedAt != null;
    }
}
