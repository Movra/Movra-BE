package com.example.movra.application.day_closing;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.day_closing.application.service.ClosedBy;
import com.example.movra.bc.day_closing.application.service.DayClosingOrchestrator;
import com.example.movra.bc.focus.focus_session.application.service.CloseDailyFocusService;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.CloseDailyTopPicksService;
import com.example.movra.bc.planning.timetable.application.service.CloseDailyTimetableService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DayClosingOrchestratorTest {

    @InjectMocks
    private DayClosingOrchestrator dayClosingOrchestrator;

    @Mock
    private CloseDailyFocusService closeDailyFocusService;

    @Mock
    private CloseDailyTopPicksService closeDailyTopPicksService;

    @Mock
    private CloseDailyTimetableService closeDailyTimetableService;

    private final UserId userId = UserId.newId();
    private final LocalDate date = LocalDate.of(2026, 4, 14);

    @Test
    @DisplayName("closeUserDay invokes all three close services")
    void closeUserDay_invokesAllServices() {
        // when
        dayClosingOrchestrator.closeUserDay(userId, date, ClosedBy.USER_ACTION);

        // then
        verify(closeDailyFocusService).close(eq(userId), eq(date), any());
        verify(closeDailyTopPicksService).close(eq(userId), eq(date), any());
        verify(closeDailyTimetableService).close(eq(userId), eq(date), any());
    }

    @Test
    @DisplayName("closeUserDay continues with subsequent services when one fails")
    void closeUserDay_continuesOnFailure() {
        // given
        willThrow(new RuntimeException("focus boom"))
                .given(closeDailyFocusService).close(any(), any(), any());

        // when
        dayClosingOrchestrator.closeUserDay(userId, date, ClosedBy.SCHEDULER);

        // then
        verify(closeDailyTopPicksService).close(eq(userId), eq(date), any());
        verify(closeDailyTimetableService).close(eq(userId), eq(date), any());
    }
}
