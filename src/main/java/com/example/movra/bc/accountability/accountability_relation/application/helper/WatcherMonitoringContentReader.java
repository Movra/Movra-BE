package com.example.movra.bc.accountability.accountability_relation.application.helper;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.support.DailyFocusSummaryReader;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryItemView;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryView;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.DailyTopPicksReader;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryItemView;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryView;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.application.service.support.DailyTimetableSummaryReader;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryItemView;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryView;
import com.example.movra.bc.planning.timetable.domain.Slot;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatcherMonitoringContentReader {

    private final DailyFocusSummaryReader dailyFocusSummaryReader;
    private final DailyTopPicksReader dailyTopPicksReader;
    private final DailyTimetableSummaryReader dailyTimetableSummaryReader;
    private final FocusSessionRepository focusSessionRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final TimetableRepository timetableRepository;
    private final Clock clock;

    public Optional<DailyFocusSummaryView> findFocusSessions(UserId userId, LocalDate date) {
        return dailyFocusSummaryReader.findOne(userId, date)
                .or(() -> findLiveFocusSessions(userId, date));
    }

    public Optional<DailyTopPicksSummaryView> findTopPicks(UserId userId, LocalDate date) {
        return dailyTopPicksReader.findOne(userId, date)
                .or(() -> findLiveTopPicks(userId, date));
    }

    public Optional<DailyTimetableSummaryView> findTimetableTasks(UserId userId, LocalDate date) {
        return dailyTimetableSummaryReader.findOne(userId, date)
                .or(() -> findLiveTimetableTasks(userId, date));
    }

    private Optional<DailyFocusSummaryView> findLiveFocusSessions(UserId userId, LocalDate date) {
        Instant dayStart = date.atStartOfDay(clock.getZone()).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        Instant effectiveEnd = liveEffectiveEnd(date, dayEnd);

        if (!effectiveEnd.isAfter(dayStart)) {
            return Optional.empty();
        }

        List<FocusSession> sessions = focusSessionRepository
                .findAllOverlappingPeriod(userId, dayStart, effectiveEnd).stream()
                .sorted(Comparator.comparing(FocusSession::getStartedAt)
                        .thenComparing(session -> session.getId().id()))
                .toList();

        if (sessions.isEmpty()) {
            return Optional.empty();
        }

        List<DailyFocusSummaryItemView> items = new java.util.ArrayList<>();
        long totalSeconds = 0L;
        for (int i = 0; i < sessions.size(); i++) {
            DailyFocusSummaryItemView item = toFocusItemView(sessions.get(i), dayStart, effectiveEnd, i + 1);
            items.add(item);
            totalSeconds += item.overlapSeconds();
        }

        return Optional.of(new DailyFocusSummaryView(
                userId,
                date,
                totalSeconds,
                items.size(),
                items
        ));
    }

    private Optional<DailyTopPicksSummaryView> findLiveTopPicks(UserId userId, LocalDate date) {
        return dailyPlanRepository.findByUserIdAndPlanDateWithTasks(userId, date)
                .map(dailyPlan -> {
                    List<Task> topPicks = dailyPlan.getTasks().stream()
                            .filter(Task::isTopPicked)
                            .sorted(Comparator.comparing(task -> task.getTaskId().id()))
                            .toList();

                    List<DailyTopPicksSummaryItemView> items = new java.util.ArrayList<>();
                    for (int i = 0; i < topPicks.size(); i++) {
                        Task task = topPicks.get(i);
                        items.add(new DailyTopPicksSummaryItemView(
                                task.getContent(),
                                task.isCompleted(),
                                task.getTopPickDetail().getEstimatedMinutes(),
                                task.getTopPickDetail().getMemo(),
                                i + 1
                        ));
                    }

                    return new DailyTopPicksSummaryView(
                            userId,
                            date,
                            items.size(),
                            (int) topPicks.stream().filter(Task::isCompleted).count(),
                            items
                    );
                });
    }

    private Optional<DailyTimetableSummaryView> findLiveTimetableTasks(UserId userId, LocalDate date) {
        Optional<DailyPlan> dailyPlanOptional = dailyPlanRepository.findByUserIdAndPlanDateWithTasks(userId, date);
        if (dailyPlanOptional.isEmpty()) {
            return Optional.empty();
        }

        DailyPlan dailyPlan = dailyPlanOptional.get();
        return timetableRepository.findByDailyPlanIdWithSlots(dailyPlan.getDailyPlanId())
                .map(timetable -> toTimetableView(userId, date, dailyPlan, timetable));
    }

    private Instant liveEffectiveEnd(LocalDate date, Instant dayEnd) {
        LocalDate today = LocalDate.now(clock);
        Instant now = Instant.now(clock);

        if (date.equals(today) && now.isBefore(dayEnd)) {
            return now;
        }
        return dayEnd;
    }

    private DailyFocusSummaryItemView toFocusItemView(
            FocusSession session,
            Instant periodStart,
            Instant periodEnd,
            int displayOrder
    ) {
        Instant sessionEnd = session.getEndedAt() != null ? session.getEndedAt() : periodEnd;
        Instant overlapStart = session.getStartedAt().isAfter(periodStart) ? session.getStartedAt() : periodStart;
        Instant overlapEnd = sessionEnd.isBefore(periodEnd) ? sessionEnd : periodEnd;
        long overlapSeconds = Math.max(0L, Duration.between(overlapStart, overlapEnd).getSeconds());

        return new DailyFocusSummaryItemView(
                session.getStartedAt(),
                session.getEndedAt(),
                session.getDurationSeconds(),
                overlapStart,
                overlapEnd,
                overlapSeconds,
                displayOrder
        );
    }

    private DailyTimetableSummaryView toTimetableView(
            UserId userId,
            LocalDate date,
            DailyPlan dailyPlan,
            Timetable timetable
    ) {
        Map<TaskId, Task> taskMap = dailyPlan.getTasks().stream()
                .collect(Collectors.toMap(Task::getTaskId, Function.identity()));

        List<Slot> orderedSlots = timetable.getSlots().stream()
                .sorted(Comparator.comparing(Slot::getStartTime)
                        .thenComparing(Slot::getEndTime)
                        .thenComparing(slot -> slot.getSlotId().id()))
                .toList();

        List<DailyTimetableSummaryItemView> items = new java.util.ArrayList<>();
        for (int i = 0; i < orderedSlots.size(); i++) {
            Slot slot = orderedSlots.get(i);
            Task task = taskMap.get(slot.getTaskId());
            items.add(new DailyTimetableSummaryItemView(
                    task == null ? "" : task.getContent(),
                    task != null && task.isCompleted(),
                    slot.getStartTime(),
                    slot.getEndTime(),
                    slot.isTopPick(),
                    i + 1
            ));
        }

        return new DailyTimetableSummaryView(
                userId,
                date,
                items.size(),
                (int) orderedSlots.stream()
                        .map(slot -> taskMap.get(slot.getTaskId()))
                        .filter(task -> task != null && task.isCompleted())
                        .count(),
                items
        );
    }
}
