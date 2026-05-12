package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.exception.InvalidFocusSessionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
