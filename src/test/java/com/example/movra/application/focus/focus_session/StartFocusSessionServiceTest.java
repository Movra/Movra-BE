package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.domain.user.User;
import com.example.movra.bc.account.domain.user.repository.UserRepository;
import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.exception.FocusSessionAlreadyInProgressException;
import com.example.movra.bc.focus.focus_session.application.service.StartFocusSessionService;
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
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    private User user;

    private final UserId userId = UserId.newId();
    private final Instant now = Instant.parse("2026-04-12T00:00:00Z");

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
        FocusSession focusSession = FocusSession.start(userId, now);
        given(clock.instant()).willReturn(now);
        given(userRepository.findByIdForUpdate(userId)).willReturn(Optional.of(user));
        given(focusSessionRepository.existsByUserIdAndEndedAtIsNull(userId)).willReturn(false);
        given(focusSessionRepository.save(any(FocusSession.class))).willReturn(focusSession);

        // when
        FocusSessionResponse response = startFocusSessionService.start();

        // then
        assertThat(response.inProgress()).isTrue();
        assertThat(response.endedAt()).isNull();
        assertThat(response.startedAt()).isEqualTo(now);
        assertThat(response.recordedElapsedSeconds()).isNull();
        assertThat(response.elapsedSeconds()).isZero();
        assertThat(response.focusSessionId()).isEqualTo(focusSession.getId().id());
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
        assertThatThrownBy(() -> startFocusSessionService.start())
                .isInstanceOf(FocusSessionAlreadyInProgressException.class);
    }
}
