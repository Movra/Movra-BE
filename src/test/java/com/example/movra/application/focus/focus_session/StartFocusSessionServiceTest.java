package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.exception.FocusSessionAlreadyInProgressException;
import com.example.movra.bc.focus.focus_session.application.service.StartFocusSessionService;
import com.example.movra.bc.focus.focus_session.application.service.dto.request.StartFocusSessionRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class StartFocusSessionServiceTest {

    @InjectMocks
    private StartFocusSessionService startFocusSessionService;

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
    private final Instant now = Instant.parse("2026-04-12T00:00:00Z");
    private final StartFocusSessionRequest request = new StartFocusSessionRequest(3);

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("start creates a new focus session")
    void start_success() {
        // given
        givenCurrentUser();
        FocusSession focusSession = FocusSession.start(userId, now, request.presetMinutes());
        given(clock.instant()).willReturn(now);
        given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.of(user));
        given(focusSessionRepository.existsByUserIdAndEndedAtIsNull(userId)).willReturn(false);
        given(focusSessionRepository.save(any(FocusSession.class))).willReturn(focusSession);

        // when
        FocusSessionResponse response = startFocusSessionService.start(request);

        // then
        assertThat(response.inProgress()).isTrue();
        assertThat(response.endedAt()).isNull();
        assertThat(response.startedAt()).isEqualTo(now);
        assertThat(response.recordedElapsedSeconds()).isNull();
        assertThat(response.elapsedSeconds()).isZero();
        assertThat(response.focusSessionId()).isEqualTo(focusSession.getId().id());
        assertThat(response.presetMinutes()).isEqualTo(3);
        assertThat(response.presetSeconds()).isEqualTo(180);
        assertThat(response.presetCompletionRate()).isNull();
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.FOCUS_SESSION_STARTED),
                argThat(properties ->
                        properties.get("focusSessionId").equals(focusSession.getId().id().toString())
                                && properties.get("startedAt").equals(now.toString())
                                && properties.get("presetMinutes").equals("3")
                                && properties.get("presetSeconds").equals("180")
                )
        );
    }

    @Test
    @DisplayName("start creates an unlimited session when preset minutes is null")
    void start_nullPreset_createsUnlimitedSession() {
        // given
        givenCurrentUser();
        StartFocusSessionRequest unlimitedRequest = new StartFocusSessionRequest(null);
        FocusSession focusSession = FocusSession.startUnlimited(userId, now);
        given(clock.instant()).willReturn(now);
        given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.of(user));
        given(focusSessionRepository.existsByUserIdAndEndedAtIsNull(userId)).willReturn(false);
        given(focusSessionRepository.save(any(FocusSession.class))).willReturn(focusSession);

        // when
        FocusSessionResponse response = startFocusSessionService.start(unlimitedRequest);

        // then
        assertThat(response.inProgress()).isTrue();
        assertThat(response.unlimited()).isTrue();
        assertThat(response.presetMinutes()).isNull();
        assertThat(response.presetSeconds()).isNull();
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.FOCUS_SESSION_STARTED),
                argThat(properties ->
                        properties.get("unlimited").equals("true")
                                && !properties.containsKey("presetMinutes")
                )
        );
    }

    @Test
    @DisplayName("start throws when a session is already in progress")
    void start_alreadyInProgress_throwsException() {
        // given
        givenCurrentUser();
        given(clock.instant()).willReturn(now);
        given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.of(user));
        given(focusSessionRepository.existsByUserIdAndEndedAtIsNull(userId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> startFocusSessionService.start(request))
                .isInstanceOf(FocusSessionAlreadyInProgressException.class);
    }

    @Test
    @DisplayName("start throws when preset minutes is unsupported")
    void start_invalidPreset_throwsException() {
        // given
        givenCurrentUser();
        given(clock.instant()).willReturn(now);
        given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.of(user));
        given(focusSessionRepository.existsByUserIdAndEndedAtIsNull(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> startFocusSessionService.start(new StartFocusSessionRequest(7)))
                .isInstanceOf(com.example.movra.bc.focus.focus_session.domain.exception.InvalidFocusSessionException.class);
    }
}
