package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.exception.FocusSessionNotFoundException;
import com.example.movra.bc.focus.focus_session.application.service.StopFocusSessionService;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.FocusSessionResponse;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class StopFocusSessionServiceTest {

    @InjectMocks
    private StopFocusSessionService stopFocusSessionService;

    @Mock
    private FocusSessionRepository focusSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    @Mock
    private User user;

    private final UserId userId = UserId.newId();
    private final Instant startedAt = Instant.parse("2026-04-12T00:00:00Z");
    private final Instant endedAt = Instant.parse("2026-04-12T00:30:00Z");

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("stop completes the active focus session")
    void stop_success() {
        // given
        givenCurrentUser();
        FocusSession focusSession = FocusSession.start(userId, startedAt, 25);
        given(clock.instant()).willReturn(endedAt);
        given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.of(user));
        given(focusSessionRepository.findByUserIdAndEndedAtIsNull(userId)).willReturn(Optional.of(focusSession));

        // when
        FocusSessionResponse response = stopFocusSessionService.stop();

        // then
        assertThat(response.inProgress()).isFalse();
        assertThat(response.endedAt()).isEqualTo(endedAt);
        assertThat(response.recordedElapsedSeconds()).isEqualTo(1800L);
        assertThat(response.elapsedSeconds()).isEqualTo(1800L);
        assertThat(response.presetMinutes()).isEqualTo(25);
        assertThat(response.presetSeconds()).isEqualTo(1500);
        assertThat(response.presetCompletionRate()).isEqualTo(1.2);
        assertThat(focusSession.isInProgress()).isFalse();
        assertThat(focusSession.getDurationSeconds()).isEqualTo(1800L);
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.FOCUS_SESSION_COMPLETED),
                argThat(properties ->
                        properties.get("focusSessionId").equals(focusSession.getId().id().toString())
                                && properties.get("durationSeconds").equals("1800")
                                && properties.get("endedAt").equals(endedAt.toString())
                                && properties.get("presetMinutes").equals("25")
                                && properties.get("presetSeconds").equals("1500")
                                && properties.get("presetCompletionRate").equals("1.2")
                )
        );
    }

    @Test
    @DisplayName("stop throws when no active session exists")
    void stop_notFound_throwsException() {
        // given
        givenCurrentUser();
        given(clock.instant()).willReturn(endedAt);
        given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.of(user));
        given(focusSessionRepository.findByUserIdAndEndedAtIsNull(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> stopFocusSessionService.stop())
                .isInstanceOf(FocusSessionNotFoundException.class);
    }

    @Test
    @DisplayName("stop records abandoned event when preset is not completed")
    void stop_underPreset_recordsAbandonedEvent() {
        // given
        givenCurrentUser();
        FocusSession focusSession = FocusSession.start(userId, startedAt, 25);
        Instant earlyEnd = startedAt.plusSeconds(60);
        given(clock.instant()).willReturn(earlyEnd);
        given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.of(user));
        given(focusSessionRepository.findByUserIdAndEndedAtIsNull(userId)).willReturn(Optional.of(focusSession));

        // when
        stopFocusSessionService.stop();

        // then
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.FOCUS_SESSION_ABANDONED),
                argThat(properties ->
                        properties.get("durationSeconds").equals("60")
                                && properties.get("presetCompletionRate").equals("0.04")
                )
        );
    }
}
