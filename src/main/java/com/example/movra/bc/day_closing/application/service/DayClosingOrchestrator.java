package com.example.movra.bc.day_closing.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.CloseDailyFocusService;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.CloseDailyTopPicksService;
import com.example.movra.bc.planning.timetable.application.service.CloseDailyTimetableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DayClosingOrchestrator {

    private final CloseDailyFocusService closeDailyFocusService;
    private final CloseDailyTopPicksService closeDailyTopPicksService;
    private final CloseDailyTimetableService closeDailyTimetableService;

    public void closeUserDay(UserId userId, LocalDate date, ClosedBy closedBy) {
        tryClose("focus", () -> closeDailyFocusService.close(userId, date, toFocusClosedBy(closedBy)), userId, date);
        tryClose("topPicks", () -> closeDailyTopPicksService.close(userId, date, toDailyPlanClosedBy(closedBy)), userId, date);
        tryClose("timetable", () -> closeDailyTimetableService.close(userId, date, toTimetableClosedBy(closedBy)), userId, date);
    }

    private void tryClose(String label, Runnable action, UserId userId, LocalDate date) {
        try {
            action.run();
        } catch (RuntimeException e) {
            log.error("DayClosing [{}] failed for user={}, date={}: {}",
                    label, userId.id(), date, e.getMessage(), e);
        }
    }

    private com.example.movra.bc.focus.focus_session.domain.type.ClosedBy toFocusClosedBy(ClosedBy closedBy) {
        return switch (closedBy) {
            case USER_ACTION -> com.example.movra.bc.focus.focus_session.domain.type.ClosedBy.USER_ACTION;
            case SCHEDULER -> com.example.movra.bc.focus.focus_session.domain.type.ClosedBy.SCHEDULER;
        };
    }

    private com.example.movra.bc.planning.daily_plan.domain.type.ClosedBy toDailyPlanClosedBy(ClosedBy closedBy) {
        return switch (closedBy) {
            case USER_ACTION -> com.example.movra.bc.planning.daily_plan.domain.type.ClosedBy.USER_ACTION;
            case SCHEDULER -> com.example.movra.bc.planning.daily_plan.domain.type.ClosedBy.SCHEDULER;
        };
    }

    private com.example.movra.bc.planning.timetable.domain.type.ClosedBy toTimetableClosedBy(ClosedBy closedBy) {
        return switch (closedBy) {
            case USER_ACTION -> com.example.movra.bc.planning.timetable.domain.type.ClosedBy.USER_ACTION;
            case SCHEDULER -> com.example.movra.bc.planning.timetable.domain.type.ClosedBy.SCHEDULER;
        };
    }
}
