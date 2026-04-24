package com.example.movra.bc.accountability.accountability_relation.presentation;

import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryWatcherFocusSessionService;
import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryWatcherTimetableTaskService;
import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryWatcherTopPicksService;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryView;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryView;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accountability-relations/watcher")
@RequiredArgsConstructor
public class WatcherQueryController {

    private final QueryWatcherFocusSessionService queryWatcherFocusSessionService;
    private final QueryWatcherTopPicksService queryWatcherTopPicksService;
    private final QueryWatcherTimetableTaskService queryWatcherTimetableTaskService;

    @GetMapping("/focus-sessions")
    public ResponseEntity<DailyFocusSummaryView> queryFocusSession(@RequestParam LocalDate date) {
        return queryWatcherFocusSessionService.query(date)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/focus-sessions/range")
    public List<DailyFocusSummaryView> queryFocusSessionRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return queryWatcherFocusSessionService.queryRange(from, to);
    }

    @GetMapping("/top-picks")
    public ResponseEntity<DailyTopPicksSummaryView> queryTopPicks(@RequestParam LocalDate date) {
        return queryWatcherTopPicksService.query(date)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/top-picks/range")
    public List<DailyTopPicksSummaryView> queryTopPicksRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return queryWatcherTopPicksService.queryRange(from, to);
    }

    @GetMapping("/timetable-tasks")
    public ResponseEntity<DailyTimetableSummaryView> queryTimetableTask(@RequestParam LocalDate date) {
        return queryWatcherTimetableTaskService.query(date)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/timetable-tasks/range")
    public List<DailyTimetableSummaryView> queryTimetableTaskRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return queryWatcherTimetableTaskService.queryRange(from, to);
    }
}
