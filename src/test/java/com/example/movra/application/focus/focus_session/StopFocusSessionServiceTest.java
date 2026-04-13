package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.domain.user.User;
import com.example.movra.bc.account.domain.user.repository.UserRepository;
import com.example.movra.bc.account.domain.user.vo.UserId;
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
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
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
        FocusSession focusSession = FocusSession.start(userId, startedAt);
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
        assertThat(focusSession.isInProgress()).isFalse();
        assertThat(focusSession.getDurationSeconds()).isEqualTo(1800L);
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
}
