package com.example.movra.application.insight.behavior_insight;

import com.example.movra.bc.insight.behavior_insight.application.service.support.InsightMetricsCalculator;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.AnalyticsEventView;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.FocusSessionView;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InsightMetricsCalculatorTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final InsightMetricsCalculator calculator = new InsightMetricsCalculator();

    private static Instant at(int month, int day, int hour, int minute) {
        return LocalDateTime.of(2026, month, day, hour, minute).atZone(ZONE).toInstant();
    }

    @Test
    @DisplayName("calculate_혼합이벤트와세션_지표를정확히집계한다")
    void calculate_mixedEventsAndSessions_aggregatesMetrics() {
        List<AnalyticsEventView> events = List.of(
                new AnalyticsEventView("FOCUS_SESSION_COMPLETED", at(5, 1, 9, 0), Map.of()),
                new AnalyticsEventView("FOCUS_SESSION_COMPLETED", at(5, 1, 14, 0), Map.of()),
                new AnalyticsEventView("FOCUS_SESSION_COMPLETED", at(5, 2, 9, 0), Map.of()),
                new AnalyticsEventView("FOCUS_SESSION_ABANDONED", at(5, 2, 16, 0), Map.of()),
                new AnalyticsEventView("FOCUS_SESSION_AUTO_CLOSED", at(5, 3, 10, 0), Map.of()),
                new AnalyticsEventView("TOP_PICK_SELECTED", at(5, 1, 8, 0), Map.of()),
                new AnalyticsEventView("TOP_PICK_SELECTED", at(5, 2, 8, 0), Map.of())
        );

        List<FocusSessionView> sessions = List.of(
                new FocusSessionView(at(5, 1, 9, 0), at(5, 1, 10, 0)),   // 3600s, hour 9
                new FocusSessionView(at(5, 2, 9, 30), at(5, 2, 10, 0)),  // 1800s, hour 9
                new FocusSessionView(at(5, 1, 14, 0), null)              // 진행 중 → 제외
        );

        InsightMetrics metrics = calculator.calculate(events, sessions, 4, 7, ZONE);

        assertThat(metrics.completedSessionCount()).isEqualTo(3);
        assertThat(metrics.abandonedSessionCount()).isEqualTo(2);
        assertThat(metrics.focusCompletionRate()).isEqualTo(0.6);
        assertThat(metrics.totalFocusSeconds()).isEqualTo(5400L);
        assertThat(metrics.peakFocusHour()).isEqualTo(9);
        assertThat(metrics.activeDayCount()).isEqualTo(3);
        assertThat(metrics.topPickSelectedCount()).isEqualTo(2);
        assertThat(metrics.reflectionCount()).isEqualTo(4);
        assertThat(metrics.tinyWinCount()).isEqualTo(7);
        // 중단 2건(ABANDONED, AUTO_CLOSED) 모두 24h 내 완료 재개 없음 → 0.0
        assertThat(metrics.quickResumeRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("calculate_데이터없음_0과null로집계한다")
    void calculate_noData_returnsZeroAndNull() {
        InsightMetrics metrics = calculator.calculate(List.of(), List.of(), 0, 0, ZONE);

        assertThat(metrics.completedSessionCount()).isZero();
        assertThat(metrics.abandonedSessionCount()).isZero();
        assertThat(metrics.focusCompletionRate()).isZero();
        assertThat(metrics.totalFocusSeconds()).isZero();
        assertThat(metrics.peakFocusHour()).isNull();
        assertThat(metrics.activeDayCount()).isZero();
        assertThat(metrics.topPickSelectedCount()).isZero();
        // 중단이 없으면 재개율은 평가 불가 → null
        assertThat(metrics.quickResumeRate()).isNull();
    }
}
