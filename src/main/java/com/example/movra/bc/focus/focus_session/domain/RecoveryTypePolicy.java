package com.example.movra.bc.focus.focus_session.domain;

import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;

/**
 * 어제의 집중/탑픽 수행 결과, 최근 시험 직후 여부, 마지막 세션 이후 경과일을 바탕으로
 * 회복 카드의 {@link RecoveryType}을 결정하는 순수 도메인 정책.
 * <p>
 * 외부 BC 타입에 의존하지 않도록 판정에 필요한 사실(fact)만 원시 값으로 받는다.
 */
public final class RecoveryTypePolicy {

    /** 마지막 세션 이후 이 일수 이상이면 장기 미접속(LONG_ABSENCE)으로 본다. */
    public static final long LONG_ABSENCE_THRESHOLD_DAYS = 7L;

    private RecoveryTypePolicy() {
    }

    public static RecoveryType determine(
            boolean missedFocus,
            boolean incompleteTopPick,
            boolean hasRecentPostExam,
            Long daysSinceLastSession
    ) {
        if (hasRecentPostExam) {
            return RecoveryType.POST_EXAM_RECOVERY;
        }
        if (daysSinceLastSession != null && daysSinceLastSession >= LONG_ABSENCE_THRESHOLD_DAYS) {
            return RecoveryType.LONG_ABSENCE;
        }
        if (missedFocus && incompleteTopPick) {
            return RecoveryType.BOTH;
        }
        if (missedFocus) {
            return RecoveryType.MISSED_FOCUS;
        }
        if (incompleteTopPick) {
            return RecoveryType.INCOMPLETE_TOP_PICK;
        }
        return RecoveryType.NONE;
    }
}
