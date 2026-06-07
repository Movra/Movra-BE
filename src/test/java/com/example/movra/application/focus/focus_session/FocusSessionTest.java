package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.exception.InvalidFocusSessionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FocusSessionTest {

    private final UserId userId = UserId.newId();
    private final Instant startedAt = Instant.parse("2026-04-12T00:00:00Z");

    @Test
    @DisplayName("start throws when userId is null")
    void start_userIdIsNull_throwsException() {
        assertThatThrownBy(() -> FocusSession.start(null, startedAt))
                .isInstanceOf(InvalidFocusSessionException.class);
    }

    @Test
    @DisplayName("start throws when startedAt is null")
    void start_startedAtIsNull_throwsException() {
        assertThatThrownBy(() -> FocusSession.start(userId, null))
                .isInstanceOf(InvalidFocusSessionException.class);
    }

    @Test
    @DisplayName("start stores preset minutes")
    void start_presetMinutes_success() {
        FocusSession focusSession = FocusSession.start(userId, startedAt, 3);

        assertThat(focusSession.getPresetMinutes()).isEqualTo(3);
        assertThat(focusSession.presetSeconds()).isEqualTo(180);
    }

    @Test
    @DisplayName("start throws when preset minutes is unsupported")
    void start_unsupportedPresetMinutes_throwsException() {
        assertThatThrownBy(() -> FocusSession.start(userId, startedAt, 7))
                .isInstanceOf(InvalidFocusSessionException.class);
    }

    @Test
    @DisplayName("complete calculates preset completion rate")
    void complete_calculatesPresetCompletionRate_success() {
        FocusSession focusSession = FocusSession.start(userId, startedAt, 3);

        focusSession.complete(startedAt.plusSeconds(90));

        assertThat(focusSession.getDurationSeconds()).isEqualTo(90);
        assertThat(focusSession.presetCompletionRate()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("complete throws when endedAt is null")
    void complete_endedAtIsNull_throwsException() {
        FocusSession focusSession = FocusSession.start(userId, startedAt);

        assertThatThrownBy(() -> focusSession.complete(null))
                .isInstanceOf(InvalidFocusSessionException.class);
    }

    @Test
    @DisplayName("complete throws when endedAt is before startedAt")
    void complete_endedAtBeforeStartedAt_throwsException() {
        FocusSession focusSession = FocusSession.start(userId, startedAt);

        assertThatThrownBy(() -> focusSession.complete(startedAt.minusSeconds(1)))
                .isInstanceOf(InvalidFocusSessionException.class);
    }

    @Test
    @DisplayName("startUnlimited creates a session without a preset")
    void startUnlimited_success() {
        FocusSession focusSession = FocusSession.startUnlimited(userId, startedAt);

        assertThat(focusSession.isUnlimited()).isTrue();
        assertThat(focusSession.getPresetMinutes()).isNull();
        assertThat(focusSession.presetSeconds()).isNull();
        assertThat(focusSession.presetCompletionRate()).isNull();
        assertThat(focusSession.isInProgress()).isTrue();
    }

    @Test
    @DisplayName("startUnlimited throws when userId is null")
    void startUnlimited_userIdIsNull_throwsException() {
        assertThatThrownBy(() -> FocusSession.startUnlimited(null, startedAt))
                .isInstanceOf(InvalidFocusSessionException.class);
    }

    @Test
    @DisplayName("complete caps duration at the max session duration")
    void complete_beyondMaxDuration_capsAtMax() {
        FocusSession focusSession = FocusSession.startUnlimited(userId, startedAt);
        Instant beyondMax = startedAt.plus(FocusSession.MAX_SESSION_DURATION).plusSeconds(3600);

        focusSession.complete(beyondMax);

        long maxSeconds = FocusSession.MAX_SESSION_DURATION.getSeconds();
        assertThat(focusSession.getEndedAt()).isEqualTo(startedAt.plus(FocusSession.MAX_SESSION_DURATION));
        assertThat(focusSession.getDurationSeconds()).isEqualTo(maxSeconds);
        assertThat(focusSession.elapsedSecondsAt(beyondMax)).isEqualTo(maxSeconds);
    }

    @Test
    @DisplayName("elapsedSecondsAt caps in-progress elapsed at the max session duration")
    void elapsedSecondsAt_inProgressBeyondMax_capsAtMax() {
        FocusSession focusSession = FocusSession.startUnlimited(userId, startedAt);
        Instant farFuture = startedAt.plus(Duration.ofHours(20));

        assertThat(focusSession.elapsedSecondsAt(farFuture))
                .isEqualTo(FocusSession.MAX_SESSION_DURATION.getSeconds());
    }

    @Test
    @DisplayName("isExpiredAt is true only after the deadline for in-progress sessions")
    void isExpiredAt_afterDeadline_returnsTrue() {
        FocusSession focusSession = FocusSession.startUnlimited(userId, startedAt);
        Instant deadline = focusSession.autoCloseDeadline();

        assertThat(focusSession.isExpiredAt(deadline.minusSeconds(1))).isFalse();
        assertThat(focusSession.isExpiredAt(deadline)).isTrue();
    }

    @Test
    @DisplayName("autoClose completes the session at the deadline")
    void autoClose_completesAtDeadline() {
        FocusSession focusSession = FocusSession.startUnlimited(userId, startedAt);

        focusSession.autoClose();

        assertThat(focusSession.isInProgress()).isFalse();
        assertThat(focusSession.getEndedAt()).isEqualTo(focusSession.autoCloseDeadline());
        assertThat(focusSession.getDurationSeconds())
                .isEqualTo(FocusSession.MAX_SESSION_DURATION.getSeconds());
    }

    @Test
    @DisplayName("isExpiredAt is false once the session is completed")
    void isExpiredAt_completedSession_returnsFalse() {
        FocusSession focusSession = FocusSession.startUnlimited(userId, startedAt);
        focusSession.complete(startedAt.plusSeconds(60));

        assertThat(focusSession.isExpiredAt(startedAt.plus(Duration.ofHours(20)))).isFalse();
    }
}
