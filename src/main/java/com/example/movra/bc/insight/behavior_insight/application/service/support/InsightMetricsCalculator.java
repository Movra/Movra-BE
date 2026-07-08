package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.AnalyticsEventView;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.FocusSessionView;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightMetrics;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 포트로 수집한 원시 데이터를 LLM 없이 결정론적으로 집계해 InsightMetrics를 만든다.
 * 모든 시각 기반 집계는 주입된 zone 기준으로 일/시간을 계산한다.
 */
@Component
public class InsightMetricsCalculator {

    private static final String EVENT_FOCUS_COMPLETED = "FOCUS_SESSION_COMPLETED";
    private static final String EVENT_FOCUS_ABANDONED = "FOCUS_SESSION_ABANDONED";
    private static final String EVENT_FOCUS_AUTO_CLOSED = "FOCUS_SESSION_AUTO_CLOSED";
    private static final String EVENT_TOP_PICK_SELECTED = "TOP_PICK_SELECTED";
    private static final long QUICK_RESUME_WINDOW_HOURS = 24;

    public InsightMetrics calculate(
            List<AnalyticsEventView> events,
            List<FocusSessionView> sessions,
            int reflectionCount,
            int tinyWinCount,
            ZoneId zone
    ) {
        int completedSessionCount = countByType(events, EVENT_FOCUS_COMPLETED);
        int abandonedSessionCount =
                countByType(events, EVENT_FOCUS_ABANDONED) + countByType(events, EVENT_FOCUS_AUTO_CLOSED);

        int totalAttempts = completedSessionCount + abandonedSessionCount;
        double focusCompletionRate = totalAttempts == 0
                ? 0.0
                : (double) completedSessionCount / totalAttempts;

        return new InsightMetrics(
                totalFocusSeconds(sessions),
                completedSessionCount,
                abandonedSessionCount,
                focusCompletionRate,
                peakFocusHour(sessions, zone),
                activeDayCount(events, zone),
                countByType(events, EVENT_TOP_PICK_SELECTED),
                reflectionCount,
                tinyWinCount,
                quickResumeRate(events)
        );
    }

    /**
     * 중단/자동마감 이벤트 이후 QUICK_RESUME_WINDOW_HOURS 내에 완료 세션이 있으면 "재개"로 본다.
     * 중단이 한 건도 없으면 평가 불가이므로 null.
     */
    private Double quickResumeRate(List<AnalyticsEventView> events) {
        List<Instant> abandons = events.stream()
                .filter(event -> EVENT_FOCUS_ABANDONED.equals(event.eventType())
                        || EVENT_FOCUS_AUTO_CLOSED.equals(event.eventType()))
                .map(AnalyticsEventView::occurredAt)
                .toList();

        if (abandons.isEmpty()) {
            return null;
        }

        List<Instant> completions = events.stream()
                .filter(event -> EVENT_FOCUS_COMPLETED.equals(event.eventType()))
                .map(AnalyticsEventView::occurredAt)
                .toList();

        long resumed = abandons.stream()
                .filter(abandon -> completions.stream().anyMatch(completed ->
                        completed.isAfter(abandon)
                                && !completed.isAfter(abandon.plus(Duration.ofHours(QUICK_RESUME_WINDOW_HOURS)))))
                .count();

        return (double) resumed / abandons.size();
    }

    private int countByType(List<AnalyticsEventView> events, String eventType) {
        return (int) events.stream()
                .filter(event -> eventType.equals(event.eventType()))
                .count();
    }

    private long totalFocusSeconds(List<FocusSessionView> sessions) {
        return sessions.stream()
                .filter(FocusSessionView::isCompleted)
                .mapToLong(session -> Math.max(0L, Duration.between(session.startedAt(), session.endedAt()).getSeconds()))
                .sum();
    }

    private Integer peakFocusHour(List<FocusSessionView> sessions, ZoneId zone) {
        Map<Integer, Long> hourCounts = new HashMap<>();
        for (FocusSessionView session : sessions) {
            if (!session.isCompleted()) {
                continue;
            }
            int hour = session.startedAt().atZone(zone).getHour();
            hourCounts.merge(hour, 1L, Long::sum);
        }

        return hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private int activeDayCount(List<AnalyticsEventView> events, ZoneId zone) {
        Set<LocalDate> activeDays = new HashSet<>();
        for (AnalyticsEventView event : events) {
            activeDays.add(event.occurredAt().atZone(zone).toLocalDate());
        }
        return activeDays.size();
    }
}
