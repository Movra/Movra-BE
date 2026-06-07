package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.support.RecoveryGuidanceResolver;
import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExamTrack;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RecoveryGuidanceResolverTest {

    private final UserId userId = UserId.newId();

    private BehaviorProfile behaviorProfileWith(RecoveryStyle style, CoachingMode coachingMode) {
        return BehaviorProfile.create(
                userId,
                ExecutionDifficulty.values()[0],
                SocialPreference.values()[0],
                style,
                ExamTrack.NAESIN,
                9,
                18,
                coachingMode
        );
    }

    @Test
    @DisplayName("resolveSuggestedAction returns post-exam copy for POST_EXAM_RECOVERY")
    void resolveSuggestedAction_postExam_returnsPostExamCopy() {
        String action = RecoveryGuidanceResolver.resolveSuggestedAction(
                Optional.empty(), RecoveryType.POST_EXAM_RECOVERY);

        assertThat(action).isEqualTo("시험 직후에는 회복이 먼저예요. 오늘은 10분만 가볍게 다시 시작해볼까요?");
    }

    @Test
    @DisplayName("resolveSuggestedAction returns long-absence copy for LONG_ABSENCE")
    void resolveSuggestedAction_longAbsence_returnsLongAbsenceCopy() {
        String action = RecoveryGuidanceResolver.resolveSuggestedAction(
                Optional.empty(), RecoveryType.LONG_ABSENCE);

        assertThat(action).isEqualTo("오랜만이어도 괜찮아요. 오늘은 3분만 다시 연결해볼까요?");
    }

    @Test
    @DisplayName("resolveSuggestedAction returns null for NONE")
    void resolveSuggestedAction_none_returnsNull() {
        String action = RecoveryGuidanceResolver.resolveSuggestedAction(
                Optional.empty(), RecoveryType.NONE);

        assertThat(action).isNull();
    }

    @Test
    @DisplayName("resolveSuggestedAction falls back to neutral default when profile is absent")
    void resolveSuggestedAction_noProfile_returnsNeutralDefault() {
        String action = RecoveryGuidanceResolver.resolveSuggestedAction(
                Optional.empty(), RecoveryType.MISSED_FOCUS);

        assertThat(action).isEqualTo("다시 시작해볼까요?");
    }

    @Test
    @DisplayName("resolveSuggestedAction selects message by recovery style and coaching mode")
    void resolveSuggestedAction_styleAndMode_returnsMatchingMessage() {
        BehaviorProfile profile = behaviorProfileWith(RecoveryStyle.SLOW_REBUILDER, CoachingMode.NEUTRAL);

        String action = RecoveryGuidanceResolver.resolveSuggestedAction(
                Optional.of(profile), RecoveryType.MISSED_FOCUS);

        assertThat(action).isEqualTo("3분만 해볼까요? 작게 시작하면 돼요.");
    }

    @Test
    @DisplayName("resolveSuggestedDurationMinutes returns null for NONE")
    void resolveSuggestedDurationMinutes_none_returnsNull() {
        Integer minutes = RecoveryGuidanceResolver.resolveSuggestedDurationMinutes(
                Optional.empty(), RecoveryType.NONE);

        assertThat(minutes).isNull();
    }

    @Test
    @DisplayName("resolveSuggestedDurationMinutes returns 10 for post-exam and 3 for long-absence")
    void resolveSuggestedDurationMinutes_fixedTypes_returnFixedValues() {
        assertThat(RecoveryGuidanceResolver.resolveSuggestedDurationMinutes(
                Optional.empty(), RecoveryType.POST_EXAM_RECOVERY)).isEqualTo(10);
        assertThat(RecoveryGuidanceResolver.resolveSuggestedDurationMinutes(
                Optional.empty(), RecoveryType.LONG_ABSENCE)).isEqualTo(3);
    }

    @Test
    @DisplayName("resolveSuggestedDurationMinutes defaults to 5 when profile is absent")
    void resolveSuggestedDurationMinutes_noProfile_returnsDefault() {
        Integer minutes = RecoveryGuidanceResolver.resolveSuggestedDurationMinutes(
                Optional.empty(), RecoveryType.MISSED_FOCUS);

        assertThat(minutes).isEqualTo(5);
    }

    @Test
    @DisplayName("resolveSuggestedDurationMinutes returns 3 for SLOW_REBUILDER style")
    void resolveSuggestedDurationMinutes_slowRebuilder_returnsThree() {
        BehaviorProfile profile = behaviorProfileWith(RecoveryStyle.SLOW_REBUILDER, CoachingMode.NEUTRAL);

        Integer minutes = RecoveryGuidanceResolver.resolveSuggestedDurationMinutes(
                Optional.of(profile), RecoveryType.MISSED_FOCUS);

        assertThat(minutes).isEqualTo(3);
    }
}
