package com.example.movra.bc.focus.focus_session.application.service.support;

import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;

import java.util.Optional;

/**
 * 회복 유형과 사용자 행동 프로파일(회복 성향 · 코칭 모드)을 조합해
 * 회복 카드에 노출할 추천 문구와 추천 시간(분)을 결정한다.
 * <p>
 * 행동 프로파일은 personalization BC 소유이므로 도메인 계층이 아닌
 * 애플리케이션 계층에 위치시켜 기존 의존 방향을 유지한다.
 */
public final class RecoveryGuidanceResolver {

    private static final int DEFAULT_DURATION_MINUTES = 5;
    private static final int POST_EXAM_DURATION_MINUTES = 10;
    private static final int LONG_ABSENCE_DURATION_MINUTES = 3;

    private RecoveryGuidanceResolver() {
    }

    public static String resolveSuggestedAction(Optional<BehaviorProfile> behaviorProfile, RecoveryType recoveryType) {
        if (recoveryType == RecoveryType.POST_EXAM_RECOVERY) {
            return "시험 직후에는 회복이 먼저예요. 오늘은 10분만 가볍게 다시 시작해볼까요?";
        }

        if (recoveryType == RecoveryType.LONG_ABSENCE) {
            return "오랜만이어도 괜찮아요. 오늘은 3분만 다시 연결해볼까요?";
        }

        if (recoveryType == RecoveryType.NONE) {
            return null;
        }

        RecoveryStyle recoveryStyle = behaviorProfile.map(BehaviorProfile::getRecoveryStyle).orElse(null);
        CoachingMode coachingMode = behaviorProfile.map(BehaviorProfile::getCoachingMode).orElse(CoachingMode.NEUTRAL);

        if (recoveryStyle == null) {
            return defaultMessageFor(coachingMode);
        }

        return messageFor(recoveryStyle, coachingMode);
    }

    public static Integer resolveSuggestedDurationMinutes(Optional<BehaviorProfile> behaviorProfile, RecoveryType recoveryType) {
        if (recoveryType == RecoveryType.NONE) {
            return null;
        }

        if (recoveryType == RecoveryType.POST_EXAM_RECOVERY) {
            return POST_EXAM_DURATION_MINUTES;
        }

        if (recoveryType == RecoveryType.LONG_ABSENCE) {
            return LONG_ABSENCE_DURATION_MINUTES;
        }

        return behaviorProfile.map(BehaviorProfile::getRecoveryStyle)
                .map(RecoveryGuidanceResolver::durationForRecoveryStyle)
                .orElse(DEFAULT_DURATION_MINUTES);
    }

    private static int durationForRecoveryStyle(RecoveryStyle recoveryStyle) {
        return switch (recoveryStyle) {
            case QUICK_RESTART -> 5;
            case NEEDS_REFLECTION -> 5;
            case SLOW_REBUILDER -> 3;
        };
    }

    private static String messageFor(RecoveryStyle recoveryStyle, CoachingMode coachingMode) {
        return switch (recoveryStyle) {
            case QUICK_RESTART -> switch (coachingMode) {
                case GENTLE -> "어제는 쉬어갔어요. 준비됐을 때 가볍게 시작해볼까요?";
                case STRICT -> "준비됐어? 지금 바로 5분만 가자.";
                case NEUTRAL -> "어제는 쉬어갔어요. 지금 바로 시작해볼까요?";
            };
            case NEEDS_REFLECTION -> switch (coachingMode) {
                case GENTLE -> "어제 무엇이 힘들었는지 천천히 한 줄만 적어봐도 좋아요.";
                case STRICT -> "어제 무엇이 무너졌는지 한 줄로 정리하고 다시 시작해.";
                case NEUTRAL -> "어제 무엇이 어려웠는지 한 줄만 적어볼까요?";
            };
            case SLOW_REBUILDER -> switch (coachingMode) {
                case GENTLE -> "오늘은 3분이면 충분해요. 천천히 다시 연결해볼까요?";
                case STRICT -> "3분만. 그 정도는 지금 할 수 있어.";
                case NEUTRAL -> "3분만 해볼까요? 작게 시작하면 돼요.";
            };
        };
    }

    private static String defaultMessageFor(CoachingMode coachingMode) {
        return switch (coachingMode) {
            case GENTLE -> "괜찮아요. 오늘 다시 시작해볼까요?";
            case STRICT -> "다시 시작해. 지금이 그 시점이야.";
            case NEUTRAL -> "다시 시작해볼까요?";
        };
    }
}
