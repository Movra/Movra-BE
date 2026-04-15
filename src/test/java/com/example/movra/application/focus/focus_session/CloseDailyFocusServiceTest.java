package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.CloseDailyFocusService;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.focus.focus_session.domain.type.ClosedBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CloseDailyFocusServiceTest {

    private CloseDailyFocusService closeDailyFocusService;

    @Mock
    private DailyFocusSummaryRepository dailyFocusSummaryRepository;

    @Mock
    private FocusSessionRepository focusSessionRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final UserId userId = UserId.newId();
    private final LocalDate date = LocalDate.of(2026, 4, 14);

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        closeDailyFocusService = new CloseDailyFocusService(dailyFocusSummaryRepository, focusSessionRepository, clock);
    }

    @Test
    @DisplayName("close creates a summary aggregating completed sessions in the day")
    void close_createsSummary() {
        // given
        given(dailyFocusSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        FocusSession a = FocusSession.start(userId, Instant.parse("2026-04-14T01:00:00Z"));
        a.complete(Instant.parse("2026-04-14T01:30:00Z"));
        FocusSession b = FocusSession.start(userId, Instant.parse("2026-04-14T05:00:00Z"));
        b.complete(Instant.parse("2026-04-14T06:00:00Z"));
        given(focusSessionRepository.findCompletedByUserIdAndStartedAtIn(any(), any(), any()))
                .willReturn(List.of(a, b));

        // when
        closeDailyFocusService.close(userId, date, ClosedBy.USER_ACTION);

        // then
        ArgumentCaptor<DailyFocusSummary> captor = ArgumentCaptor.forClass(DailyFocusSummary.class);
        verify(dailyFocusSummaryRepository).save(captor.capture());
        DailyFocusSummary saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getSessionCount()).isEqualTo(2);
        assertThat(saved.getTotalSeconds()).isEqualTo(30 * 60 + 60 * 60);
        assertThat(saved.getClosedBy()).isEqualTo(ClosedBy.USER_ACTION);
    }

    @Test
    @DisplayName("close is idempotent when the summary already exists")
    void close_idempotent() {
        // given
        given(dailyFocusSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(true);

        // when
        closeDailyFocusService.close(userId, date, ClosedBy.USER_ACTION);

        // then
        verify(dailyFocusSummaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("close records zero values when there are no completed sessions")
    void close_emptyDay() {
        // given
        given(dailyFocusSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(focusSessionRepository.findCompletedByUserIdAndStartedAtIn(any(), any(), any()))
                .willReturn(List.of());

        // when
        closeDailyFocusService.close(userId, date, ClosedBy.SCHEDULER);

        // then
        ArgumentCaptor<DailyFocusSummary> captor = ArgumentCaptor.forClass(DailyFocusSummary.class);
        verify(dailyFocusSummaryRepository).save(captor.capture());
        DailyFocusSummary saved = captor.getValue();
        assertThat(saved.getSessionCount()).isZero();
        assertThat(saved.getTotalSeconds()).isZero();
        assertThat(saved.getClosedBy()).isEqualTo(ClosedBy.SCHEDULER);
    }
}
