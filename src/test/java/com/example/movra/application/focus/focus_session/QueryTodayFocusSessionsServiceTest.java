package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.QueryTodayFocusSessionsService;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.TodayFocusSessionsResponse;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QueryTodayFocusSessionsServiceTest {

    @InjectMocks
    private QueryTodayFocusSessionsService queryTodayFocusSessionsService;

    @Mock
    private FocusSessionRepository focusSessionRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    private final UserId userId = UserId.newId();
    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    private final Instant now = ZonedDateTime.of(2026, 4, 12, 9, 0, 0, 0, zoneId).toInstant();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("query returns today's focus sessions")
    void query_success() {
        // given
        givenCurrentUser();
        FocusSession completedSession = FocusSession.start(
                userId,
                ZonedDateTime.of(2026, 4, 11, 23, 50, 0, 0, zoneId).toInstant()
        );
        completedSession.complete(ZonedDateTime.of(2026, 4, 12, 0, 10, 0, 0, zoneId).toInstant());
        FocusSession activeSession = FocusSession.start(
                userId,
                ZonedDateTime.of(2026, 4, 12, 8, 0, 0, 0, zoneId).toInstant()
        );

        given(clock.instant()).willReturn(now);
        given(clock.getZone()).willReturn(zoneId);

        given(focusSessionRepository.findAllOverlappingPeriod(
                any(UserId.class),
                any(Instant.class),
                any(Instant.class)
        )).willReturn(List.of(completedSession, activeSession));

        // when
        TodayFocusSessionsResponse response = queryTodayFocusSessionsService.query();

        // then
        assertThat(response.targetDate()).isEqualTo(LocalDate.of(2026, 4, 12));
        assertThat(response.queriedAt()).isEqualTo(now);
        assertThat(response.sessions()).hasSize(2);
        assertThat(response.focusing()).isTrue();
        assertThat(response.totalFocusSeconds()).isEqualTo(4200L);
        assertThat(response.sessions().get(0).recordedElapsedSeconds()).isEqualTo(1200L);
        assertThat(response.sessions().get(1).recordedElapsedSeconds()).isNull();
        assertThat(response.sessions().get(1).elapsedSeconds()).isEqualTo(3600L);
    }
}
