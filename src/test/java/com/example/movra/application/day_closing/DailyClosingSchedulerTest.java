package com.example.movra.application.day_closing;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.day_closing.application.scheduler.DailyClosingScheduler;
import com.example.movra.bc.day_closing.application.service.ClosedBy;
import com.example.movra.bc.day_closing.application.service.DayClosingOrchestrator;
import com.example.movra.bc.focus.focus_session.application.service.query.FocusDailySummaryQueryService;
import com.example.movra.bc.planning.daily_plan.application.service.query.DailyTopPicksSummaryQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DailyClosingSchedulerTest {

    @InjectMocks
    private DailyClosingScheduler dailyClosingScheduler;

    @Mock
    private DayClosingOrchestrator dayClosingOrchestrator;

    @Mock
    private FocusDailySummaryQueryService focusDailySummaryQueryService;

    @Mock
    private DailyTopPicksSummaryQueryService dailyTopPicksSummaryQueryService;

    @Mock
    private Clock clock;

    private final LocalDate date = LocalDate.of(2026, 4, 14);

    @Test
    @DisplayName("runFor closes each unique active user exactly once")
    void runFor_dedupesUsers() {
        // given
        UserId userA = UserId.newId();
        UserId userB = UserId.newId();
        given(focusDailySummaryQueryService.findActiveUserIds(date)).willReturn(List.of(userA, userB));
        given(dailyTopPicksSummaryQueryService.findActiveUserIds(date)).willReturn(List.of(userA));

        // when
        dailyClosingScheduler.runFor(date);

        // then
        verify(dayClosingOrchestrator).closeUserDay(eq(userA), eq(date), eq(ClosedBy.SCHEDULER));
        verify(dayClosingOrchestrator).closeUserDay(eq(userB), eq(date), eq(ClosedBy.SCHEDULER));
    }

    @Test
    @DisplayName("runFor keeps processing remaining users when one user's closing throws")
    void runFor_continuesOnUserFailure() {
        // given
        UserId userA = UserId.newId();
        UserId userB = UserId.newId();
        given(focusDailySummaryQueryService.findActiveUserIds(date)).willReturn(List.of(userA, userB));
        given(dailyTopPicksSummaryQueryService.findActiveUserIds(date)).willReturn(List.of());
        willThrow(new RuntimeException("boom"))
                .given(dayClosingOrchestrator).closeUserDay(eq(userA), any(), any());

        // when
        dailyClosingScheduler.runFor(date);

        // then
        verify(dayClosingOrchestrator).closeUserDay(eq(userB), eq(date), eq(ClosedBy.SCHEDULER));
    }
}
