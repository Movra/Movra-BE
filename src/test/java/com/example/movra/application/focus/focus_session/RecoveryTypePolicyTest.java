package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.focus.focus_session.domain.RecoveryTypePolicy;
import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecoveryTypePolicyTest {

    @Test
    @DisplayName("determine returns POST_EXAM_RECOVERY when a recent post-exam exists regardless of other facts")
    void determine_hasRecentPostExam_returnsPostExamRecovery() {
        RecoveryType result = RecoveryTypePolicy.determine(true, true, true, 10L);

        assertThat(result).isEqualTo(RecoveryType.POST_EXAM_RECOVERY);
    }

    @Test
    @DisplayName("determine returns LONG_ABSENCE when days since last session reaches the threshold")
    void determine_daysSinceLastSessionAtThreshold_returnsLongAbsence() {
        RecoveryType result = RecoveryTypePolicy.determine(false, false, false,
                RecoveryTypePolicy.LONG_ABSENCE_THRESHOLD_DAYS);

        assertThat(result).isEqualTo(RecoveryType.LONG_ABSENCE);
    }

    @Test
    @DisplayName("determine does not return LONG_ABSENCE just below the threshold")
    void determine_daysSinceLastSessionBelowThreshold_doesNotReturnLongAbsence() {
        RecoveryType result = RecoveryTypePolicy.determine(true, false, false,
                RecoveryTypePolicy.LONG_ABSENCE_THRESHOLD_DAYS - 1);

        assertThat(result).isEqualTo(RecoveryType.MISSED_FOCUS);
    }

    @Test
    @DisplayName("determine returns BOTH when focus missed and top pick incomplete")
    void determine_missedFocusAndIncompleteTopPick_returnsBoth() {
        RecoveryType result = RecoveryTypePolicy.determine(true, true, false, null);

        assertThat(result).isEqualTo(RecoveryType.BOTH);
    }

    @Test
    @DisplayName("determine returns MISSED_FOCUS when only focus missed")
    void determine_onlyMissedFocus_returnsMissedFocus() {
        RecoveryType result = RecoveryTypePolicy.determine(true, false, false, null);

        assertThat(result).isEqualTo(RecoveryType.MISSED_FOCUS);
    }

    @Test
    @DisplayName("determine returns INCOMPLETE_TOP_PICK when only top pick incomplete")
    void determine_onlyIncompleteTopPick_returnsIncompleteTopPick() {
        RecoveryType result = RecoveryTypePolicy.determine(false, true, false, null);

        assertThat(result).isEqualTo(RecoveryType.INCOMPLETE_TOP_PICK);
    }

    @Test
    @DisplayName("determine returns NONE when nothing needs recovery")
    void determine_nothingToRecover_returnsNone() {
        RecoveryType result = RecoveryTypePolicy.determine(false, false, false, null);

        assertThat(result).isEqualTo(RecoveryType.NONE);
    }
}
