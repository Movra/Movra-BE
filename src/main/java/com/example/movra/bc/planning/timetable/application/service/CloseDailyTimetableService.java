package com.example.movra.bc.planning.timetable.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.query.DailyPlanLookupService;
import com.example.movra.bc.planning.daily_plan.application.service.query.TaskCompletionQueryService;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.Slot;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.repository.DailyTimetableSummaryRepository;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.type.ClosedBy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CloseDailyTimetableService {

    private final DailyTimetableSummaryRepository dailyTimetableSummaryRepository;
    private final TimetableRepository timetableRepository;
    private final DailyPlanLookupService dailyPlanLookupService;
    private final TaskCompletionQueryService taskCompletionQueryService;
    private final Clock clock;

    @Transactional
    public void close(UserId userId, LocalDate date, ClosedBy closedBy) {
        if (dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)) {
            return;
        }

        int totalCount = 0;
        int completedCount = 0;

        Optional<DailyPlanId> planId = dailyPlanLookupService.findIdByUserAndDate(userId, date);
        if (planId.isPresent()) {
            Optional<Timetable> timetableOpt = timetableRepository.findByDailyPlanId(planId.get());
            if (timetableOpt.isPresent()) {
                List<Slot> slots = timetableOpt.get().getSlots();
                List<TaskId> taskIds = slots.stream().map(Slot::getTaskId).toList();
                totalCount = taskIds.size();

                Map<TaskId, Boolean> completionMap =
                        taskCompletionQueryService.findCompletionByTaskIds(taskIds);
                completedCount = (int) completionMap.values().stream().filter(Boolean::booleanValue).count();
            }
        }

        DailyTimetableSummary summary = DailyTimetableSummary.close(
                userId, date, totalCount, completedCount, closedBy, clock
        );
        dailyTimetableSummaryRepository.save(summary);
    }
}
