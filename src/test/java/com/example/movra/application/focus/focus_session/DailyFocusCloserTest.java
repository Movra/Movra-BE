package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.support.DailyFocusCloser;
import com.example.movra.bc.focus.focus_session.application.service.support.DailyFocusSummarySaver;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
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
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DailyFocusCloserTest {

    private DailyFocusCloser dailyFocusCloser;

    @Mock
    private DailyFocusSummaryRepository dailyFocusSummaryRepository;

    @Mock
    private FocusSessionRepository focusSessionRepository;

    @Mock
    private DailyFocusSummarySaver dailyFocusSummarySaver;

    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), zoneId);
    private final UserId userId = UserId.newId();
    private final LocalDate date = LocalDate.of(2026, 4, 14);

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        dailyFocusCloser = new DailyFocusCloser(
                dailyFocusSummaryRepository, focusSessionRepository, dailyFocusSummarySaver, clock
        );
    }

    @Test
    @DisplayName("close snapshots overlapping sessions using day overlap seconds")
    void close_createsSummary() {
        given(dailyFocusSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        FocusSession crossDayCompleted = session(
                local(2026, 4, 13, 23, 50),
                local(2026, 4, 14, 0, 10)
        );
        FocusSession sameDayCompleted = session(
                local(2026, 4, 14, 10, 0),
                local(2026, 4, 14, 10, 30)
        );
        FocusSession sameDayInProgress = session(
                local(2026, 4, 14, 23, 30),
                null
        );
        given(focusSessionRepository.findAllOverlappingPeriod(any(), any(), any()))
                .willReturn(List.of(crossDayCompleted, sameDayCompleted, sameDayInProgress));
        given(dailyFocusSummarySaver.save(any())).willReturn(true);

        dailyFocusCloser.close(userId, date);

        ArgumentCaptor<DailyFocusSummary> captor = ArgumentCaptor.forClass(DailyFocusSummary.class);
        verify(dailyFocusSummarySaver).save(captor.capture());
        DailyFocusSummary saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getSessionCount()).isEqualTo(3);
        assertThat(saved.getTotalSeconds()).isEqualTo(4200L);
        assertThat(saved.getTotalSeconds()).isEqualTo(
                saved.getItems().stream()
                        .mapToLong(item -> item.getOverlapSeconds())
                        .sum()
        );
        assertThat(saved.getItems()).hasSize(3);

        var first = saved.getItems().get(0);
        assertThat(first.getStartedAtSnapshot()).isEqualTo(local(2026, 4, 13, 23, 50));
        assertThat(first.getEndedAtSnapshot()).isEqualTo(local(2026, 4, 14, 0, 10));
        assertThat(first.getRecordedDurationSecondsSnapshot()).isEqualTo(1200L);
        assertThat(first.getOverlapStartedAt()).isEqualTo(local(2026, 4, 14, 0, 0));
        assertThat(first.getOverlapEndedAt()).isEqualTo(local(2026, 4, 14, 0, 10));
        assertThat(first.getOverlapSeconds()).isEqualTo(600L);
        assertThat(first.getDisplayOrder()).isEqualTo(1);

        var third = saved.getItems().get(2);
        assertThat(third.getStartedAtSnapshot()).isEqualTo(local(2026, 4, 14, 23, 30));
        assertThat(third.getEndedAtSnapshot()).isNull();
        assertThat(third.getRecordedDurationSecondsSnapshot()).isNull();
        assertThat(third.getOverlapStartedAt()).isEqualTo(local(2026, 4, 14, 23, 30));
        assertThat(third.getOverlapEndedAt()).isEqualTo(local(2026, 4, 15, 0, 0));
        assertThat(third.getOverlapSeconds()).isEqualTo(1800L);
        assertThat(third.getDisplayOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("close is idempotent when the summary already exists")
    void close_idempotent() {
        given(dailyFocusSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(true);

        dailyFocusCloser.close(userId, date);

        verify(dailyFocusSummarySaver, never()).save(any());
    }

    @Test
    @DisplayName("close skips when there are no sessions overlapping the day")
    void close_emptyDay() {
        given(dailyFocusSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(focusSessionRepository.findAllOverlappingPeriod(any(), any(), any()))
                .willReturn(List.of());

        dailyFocusCloser.close(userId, date);

        verify(dailyFocusSummarySaver, never()).save(any());
    }

    @Test
    @DisplayName("close treats duplicate writes as idempotent success")
    void close_duplicateAtWrite_isIgnored() {
        given(dailyFocusSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(focusSessionRepository.findAllOverlappingPeriod(any(), any(), any()))
                .willReturn(List.of(session(local(2026, 4, 14, 10, 0), local(2026, 4, 14, 10, 30))));
        given(dailyFocusSummarySaver.save(any())).willReturn(false);

        assertThatCode(() -> dailyFocusCloser.close(userId, date))
                .doesNotThrowAnyException();
    }

    private FocusSession session(Instant startedAt, Instant endedAt) {
        FocusSession session = FocusSession.start(userId, startedAt);
        if (endedAt != null) {
            session.complete(endedAt);
        }
        return session;
    }

    private Instant local(int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zoneId).toInstant();
    }
}
