package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.service.support.FocusSessionExpirer;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.focus.focus_session.domain.vo.FocusSessionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FocusSessionExpirerTest {

    @InjectMocks
    private FocusSessionExpirer focusSessionExpirer;

    @Mock
    private FocusSessionRepository focusSessionRepository;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    @Mock
    private Clock clock;

    private final UserId userId = UserId.newId();
    private final Instant startedAt = Instant.parse("2026-04-12T00:00:00Z");

    @Test
    @DisplayName("expire auto-closes an in-progress session past the deadline")
    void expire_expiredSession_autoCloses() {
        FocusSession focusSession = FocusSession.startUnlimited(userId, startedAt);
        FocusSessionId id = focusSession.getId();
        Instant afterDeadline = focusSession.autoCloseDeadline().plusSeconds(60);
        given(clock.instant()).willReturn(afterDeadline);
        given(focusSessionRepository.findById(id)).willReturn(Optional.of(focusSession));

        boolean closed = focusSessionExpirer.expire(id);

        assertThat(closed).isTrue();
        assertThat(focusSession.isInProgress()).isFalse();
        assertThat(focusSession.getEndedAt()).isEqualTo(focusSession.autoCloseDeadline());
        then(focusSessionRepository).should().save(focusSession);
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.FOCUS_SESSION_AUTO_CLOSED),
                any()
        );
    }

    @Test
    @DisplayName("expire skips a session that has not reached the deadline")
    void expire_notExpired_skips() {
        FocusSession focusSession = FocusSession.startUnlimited(userId, startedAt);
        FocusSessionId id = focusSession.getId();
        Instant beforeDeadline = focusSession.autoCloseDeadline().minusSeconds(60);
        given(clock.instant()).willReturn(beforeDeadline);
        given(focusSessionRepository.findById(id)).willReturn(Optional.of(focusSession));

        boolean closed = focusSessionExpirer.expire(id);

        assertThat(closed).isFalse();
        assertThat(focusSession.isInProgress()).isTrue();
        then(focusSessionRepository).should(never()).save(any(FocusSession.class));
        then(analyticsEventRecorder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("expire returns false when the session no longer exists")
    void expire_missingSession_returnsFalse() {
        FocusSessionId id = FocusSessionId.newId();
        given(clock.instant()).willReturn(startedAt.plus(Duration.ofHours(20)));
        given(focusSessionRepository.findById(id)).willReturn(Optional.empty());

        boolean closed = focusSessionExpirer.expire(id);

        assertThat(closed).isFalse();
        then(focusSessionRepository).should(never()).save(any(FocusSession.class));
        then(analyticsEventRecorder).shouldHaveNoInteractions();
    }
}
