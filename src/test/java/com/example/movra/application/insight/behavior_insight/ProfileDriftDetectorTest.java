package com.example.movra.application.insight.behavior_insight;

import com.example.movra.bc.insight.behavior_insight.application.service.support.ProfileDriftDetector;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.BehaviorProfileView;
import com.example.movra.bc.insight.behavior_insight.domain.event.ProfileDriftItem;
import com.example.movra.bc.insight.behavior_insight.domain.type.DriftType;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileDriftDetectorTest {

    private final ProfileDriftDetector detector = new ProfileDriftDetector();

    private InsightMetrics metrics(int completed, int abandoned, double completionRate, Integer peakHour, Double quickResumeRate) {
        return new InsightMetrics(0L, completed, abandoned, completionRate, peakHour, 10, 0, 0, 0, quickResumeRate);
    }

    @Test
    @DisplayName("detect_세가지괴리모두_세건을정확히반환한다")
    void detect_allThreeDrifts_returnsThreeFindings() {
        BehaviorProfileView profile = new BehaviorProfileView(9, 11, "NEUTRAL", "QUICK_RESTART", "HIGH");
        InsightMetrics metrics = metrics(2, 4, 0.33, 21, 0.1);

        List<ProfileDriftItem> findings = detector.detect(metrics, profile);

        Map<DriftType, ProfileDriftItem> byType = findings.stream()
                .collect(java.util.stream.Collectors.toMap(ProfileDriftItem::type, Function.identity()));

        assertThat(findings).hasSize(3);
        assertThat(byType.get(DriftType.FOCUS_HOURS).suggestedStartHour()).isEqualTo(21);
        assertThat(byType.get(DriftType.FOCUS_HOURS).suggestedEndHour()).isEqualTo(23);
        assertThat(byType.get(DriftType.EXECUTION_DIFFICULTY).suggestedValue()).isEqualTo("MEDIUM");
        assertThat(byType.get(DriftType.RECOVERY_STYLE).suggestedValue()).isEqualTo("SLOW_REBUILDER");
    }

    @Test
    @DisplayName("detect_선언과실제가일치_괴리없음")
    void detect_aligned_returnsEmpty() {
        BehaviorProfileView profile = new BehaviorProfileView(20, 22, "NEUTRAL", "SLOW_REBUILDER", "HIGH");
        InsightMetrics metrics = metrics(3, 3, 0.5, 21, 0.1);

        List<ProfileDriftItem> findings = detector.detect(metrics, profile);

        assertThat(findings).isEmpty();
    }

    @Test
    @DisplayName("detect_프로필없음_괴리없음")
    void detect_noProfile_returnsEmpty() {
        InsightMetrics metrics = metrics(3, 3, 0.2, 21, 0.1);

        List<ProfileDriftItem> findings = detector.detect(metrics, null);

        assertThat(findings).isEmpty();
    }
}
