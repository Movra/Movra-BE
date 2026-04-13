package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.helper.FocusSessionTimeCalculator;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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

    @Spy
    private FocusSessionTimeCalculator focusSessionTimeCalculator;

    private final UserId userId = UserId.newId();
    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    private final Instant now = ZonedDateTime.of(2026, 4, 12, 0, 30, 0, 0, zoneId).toInstant();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("query returns today's focus sessions")
    void query_success() {
        givenCurrentUser();
        FocusSession crossDayCompletedSession = FocusSession.start(
                userId,
                ZonedDateTime.of(2026, 4, 11, 23, 50, 0, 0, zoneId).toInstant()
        );
        crossDayCompletedSession.complete(ZonedDateTime.of(2026, 4, 12, 0, 10, 0, 0, zoneId).toInstant());
        FocusSession crossDayActiveSession = FocusSession.start(
                userId,
                ZonedDateTime.of(2026, 4, 11, 23, 55, 0, 0, zoneId).toInstant()
        );
        FocusSession todayStartedSession = FocusSession.start(
                userId,
                ZonedDateTime.of(2026, 4, 12, 0, 15, 0, 0, zoneId).toInstant()
        );

        given(clock.instant()).willReturn(now);
        given(clock.getZone()).willReturn(zoneId);

        given(focusSessionRepository.findAllOverlappingPeriod(
                any(UserId.class),
                any(Instant.class),
                any(Instant.class)
        )).willReturn(List.of(crossDayCompletedSession, crossDayActiveSession, todayStartedSession));

        TodayFocusSessionsResponse response = queryTodayFocusSessionsService.query();

        assertThat(response.targetDate()).isEqualTo(LocalDate.of(2026, 4, 12));
        assertThat(response.queriedAt()).isEqualTo(now);
        assertThat(response.sessions()).hasSize(3);
        assertThat(response.focusing()).isTrue();
        assertThat(response.totalFocusSeconds()).isEqualTo(3300L);
        assertThat(response.sessions().get(0).recordedElapsedSeconds()).isEqualTo(1200L);
        assertThat(response.sessions().get(0).elapsedSeconds()).isEqualTo(1200L);
        assertThat(response.sessions().get(1).recordedElapsedSeconds()).isNull();
        assertThat(response.sessions().get(1).elapsedSeconds()).isEqualTo(2100L);
        assertThat(response.sessions().get(2).recordedElapsedSeconds()).isNull();
        assertThat(response.sessions().get(2).elapsedSeconds()).isEqualTo(900L);
        then(focusSessionRepository).should().findAllOverlappingPeriod(
                eq(userId),
                eq(ZonedDateTime.of(2026, 4, 12, 0, 0, 0, 0, zoneId).toInstant()),
                eq(ZonedDateTime.of(2026, 4, 13, 0, 0, 0, 0, zoneId).toInstant())
        );
    }

    @Test
    @DisplayName("query derives today from the same instant snapshot")
    void query_usesSingleTimeSnapshot() {
        givenCurrentUser();
        Instant justBeforeMidnight = ZonedDateTime.of(2026, 4, 12, 23, 59, 59, 0, zoneId).toInstant();
        Instant justAfterMidnight = ZonedDateTime.of(2026, 4, 13, 0, 0, 0, 0, zoneId).toInstant();

        given(clock.instant()).willReturn(justBeforeMidnight, justAfterMidnight);
        given(clock.getZone()).willReturn(zoneId);
        given(focusSessionRepository.findAllOverlappingPeriod(
                any(UserId.class),
                any(Instant.class),
                any(Instant.class)
        )).willReturn(List.of());

        TodayFocusSessionsResponse response = queryTodayFocusSessionsService.query();

        assertThat(response.queriedAt()).isEqualTo(justBeforeMidnight);
        assertThat(response.targetDate()).isEqualTo(LocalDate.of(2026, 4, 12));
        then(focusSessionRepository).should().findAllOverlappingPeriod(
                eq(userId),
                eq(ZonedDateTime.of(2026, 4, 12, 0, 0, 0, 0, zoneId).toInstant()),
                eq(ZonedDateTime.of(2026, 4, 13, 0, 0, 0, 0, zoneId).toInstant())
        );
    }
}
