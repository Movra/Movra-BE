package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.BehaviorProfileView;
import com.example.movra.bc.insight.behavior_insight.domain.event.ProfileDriftItem;
import com.example.movra.bc.insight.behavior_insight.domain.type.DriftType;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightMetrics;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 선언된 선호(BehaviorProfileView)와 실제 행동 지표(InsightMetrics)를 결정론적으로 비교해 괴리를 감지한다.
 * LLM은 여기서 만든 결과를 "서술"만 하고, 판정 자체는 코드가 책임진다.
 */
@Component
public class ProfileDriftDetector {

    private static final int MIN_SESSIONS_FOR_DIFFICULTY = 5;
    private static final double LOW_COMPLETION = 0.4;
    private static final double VERY_LOW_COMPLETION = 0.3;
    private static final double HIGH_COMPLETION = 0.8;
    private static final double VERY_HIGH_COMPLETION = 0.9;
    private static final double QUICK_RESUME_HIGH = 0.6;
    private static final double QUICK_RESUME_LOW = 0.3;

    public List<ProfileDriftItem> detect(InsightMetrics metrics, BehaviorProfileView profile) {
        if (metrics == null || profile == null) {
            return List.of();
        }

        List<ProfileDriftItem> findings = new ArrayList<>();
        detectFocusHours(metrics, profile).ifPresent(findings::add);
        detectExecutionDifficulty(metrics, profile).ifPresent(findings::add);
        detectRecoveryStyle(metrics, profile).ifPresent(findings::add);
        return findings;
    }

    private Optional<ProfileDriftItem> detectFocusHours(InsightMetrics metrics, BehaviorProfileView profile) {
        Integer peak = metrics.peakFocusHour();
        if (peak == null) {
            return Optional.empty();
        }

        int start = profile.preferredFocusStartHour();
        int end = profile.preferredFocusEndHour();
        if (isWithinWindow(peak, start, end)) {
            return Optional.empty();
        }

        int width = windowWidth(start, end);
        int suggestedStart = peak;
        int suggestedEnd = Math.min(23, peak + width);

        String message = "선언한 집중 시간대(%d–%d시)와 달리 실제로는 %d시대에 가장 집중했어요. 시간대 조정을 제안해요."
                .formatted(start, end, peak);

        return Optional.of(new ProfileDriftItem(
                DriftType.FOCUS_HOURS,
                "%d–%d시".formatted(start, end),
                "%d시".formatted(peak),
                suggestedStart,
                suggestedEnd,
                null,
                message
        ));
    }

    private Optional<ProfileDriftItem> detectExecutionDifficulty(InsightMetrics metrics, BehaviorProfileView profile) {
        int totalAttempts = metrics.completedSessionCount() + metrics.abandonedSessionCount();
        if (totalAttempts < MIN_SESSIONS_FOR_DIFFICULTY) {
            return Optional.empty();
        }

        String declared = profile.executionDifficulty();
        double rate = metrics.focusCompletionRate();
        String suggested = suggestedDifficulty(declared, rate);
        if (suggested == null) {
            return Optional.empty();
        }

        boolean overestimated = isHarder(declared, suggested);
        String message = overestimated
                ? "설정한 난이도(%s)에 비해 완료율이 %.0f%%로 낮아요. 난이도를 %s로 낮춰 부담을 줄여볼까요?"
                        .formatted(declared, rate * 100, suggested)
                : "설정한 난이도(%s)보다 완료율이 %.0f%%로 높아요. 난이도를 %s로 올려 더 도전해볼까요?"
                        .formatted(declared, rate * 100, suggested);

        return Optional.of(new ProfileDriftItem(
                DriftType.EXECUTION_DIFFICULTY,
                declared,
                "완료율 %.0f%%".formatted(rate * 100),
                null,
                null,
                suggested,
                message
        ));
    }

    private Optional<ProfileDriftItem> detectRecoveryStyle(InsightMetrics metrics, BehaviorProfileView profile) {
        Double rate = metrics.quickResumeRate();
        if (rate == null) {
            return Optional.empty();
        }

        String observed = observedRecoveryStyle(rate);
        String declared = profile.recoveryStyle();
        if (observed.equals(declared)) {
            return Optional.empty();
        }

        String message = "선언한 회복 스타일(%s)과 달리 실제 재개 패턴은 %s에 가까워요(24h 내 재개율 %.0f%%)."
                .formatted(declared, observed, rate * 100);

        return Optional.of(new ProfileDriftItem(
                DriftType.RECOVERY_STYLE,
                declared,
                observed,
                null,
                null,
                observed,
                message
        ));
    }

    private boolean isWithinWindow(int hour, int start, int end) {
        if (start <= end) {
            return hour >= start && hour <= end;
        }
        return hour >= start || hour <= end;
    }

    private int windowWidth(int start, int end) {
        return start <= end ? (end - start) : (end + 24 - start);
    }

    private String suggestedDifficulty(String declared, double rate) {
        return switch (declared) {
            case "HIGH" -> rate < LOW_COMPLETION ? "MEDIUM" : null;
            case "MEDIUM" -> rate < VERY_LOW_COMPLETION ? "LOW" : (rate > VERY_HIGH_COMPLETION ? "HIGH" : null);
            case "LOW" -> rate > HIGH_COMPLETION ? "MEDIUM" : null;
            default -> null;
        };
    }

    private boolean isHarder(String from, String to) {
        return rank(to) < rank(from);
    }

    private int rank(String difficulty) {
        return switch (difficulty) {
            case "LOW" -> 1;
            case "MEDIUM" -> 2;
            case "HIGH" -> 3;
            default -> 0;
        };
    }

    private String observedRecoveryStyle(double quickResumeRate) {
        if (quickResumeRate >= QUICK_RESUME_HIGH) {
            return "QUICK_RESTART";
        }
        if (quickResumeRate <= QUICK_RESUME_LOW) {
            return "SLOW_REBUILDER";
        }
        return "NEEDS_REFLECTION";
    }
}
