package com.example.movra.bc.insight.behavior_insight.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;

/**
 * 결정론적으로 계산된 행동 지표 묶음. LLM 개입 없이 코드로만 산출한다.
 * peakFocusHour는 집중 세션이 없으면 null일 수 있다.
 */
@Embeddable
public record InsightMetrics(
        @Column(name = "total_focus_seconds", nullable = false)
        long totalFocusSeconds,

        @Column(name = "completed_session_count", nullable = false)
        int completedSessionCount,

        @Column(name = "abandoned_session_count", nullable = false)
        int abandonedSessionCount,

        @Column(name = "focus_completion_rate", nullable = false)
        double focusCompletionRate,

        @Column(name = "peak_focus_hour")
        Integer peakFocusHour,

        @Column(name = "active_day_count", nullable = false)
        int activeDayCount,

        @Column(name = "top_pick_selected_count", nullable = false)
        int topPickSelectedCount,

        @Column(name = "reflection_count", nullable = false)
        int reflectionCount,

        @Column(name = "tiny_win_count", nullable = false)
        int tinyWinCount,

        /** 중단/자동마감 후 24시간 내 완료 세션으로 재개한 비율. 중단이 없으면 null. */
        @Column(name = "quick_resume_rate")
        Double quickResumeRate
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
