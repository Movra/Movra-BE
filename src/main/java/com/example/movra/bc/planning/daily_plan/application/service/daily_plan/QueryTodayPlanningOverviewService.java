package com.example.movra.bc.planning.daily_plan.application.service.daily_plan;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response.TodayPlanningOverviewResponse;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.response.TopPicksResponse;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.timetable.application.service.dto.response.TimetableResponse;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryTodayPlanningOverviewService {

    private final DailyPlanRepository dailyPlanRepository;
    private final TimetableRepository timetableRepository;
    private final TodayDailyPlanProvisioner todayDailyPlanProvisioner;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional(readOnly = true)
    public TodayPlanningOverviewResponse query() {
        UserId userId = currentUserQuery.currentUser().userId();
        LocalDate today = LocalDate.now(clock);
        DailyPlan dailyPlan = dailyPlanRepository.findByUserIdAndPlanDateWithTasks(userId, today)
                .orElseGet(() -> todayDailyPlanProvisioner.createOrLoadToday(userId, today));

        return TodayPlanningOverviewResponse.builder()
                .dailyPlanId(dailyPlan.getDailyPlanId().id())
                .targetDate(dailyPlan.getPlanDate())
                .topPicks(topPicks(dailyPlan))
                .timetable(timetable(dailyPlan))
                .build();
    }

    private List<TopPicksResponse> topPicks(DailyPlan dailyPlan) {
        return dailyPlan.getTasks().stream()
                .filter(Task::isTopPicked)
                .map(TopPicksResponse::from)
                .toList();
    }

    private TimetableResponse timetable(DailyPlan dailyPlan) {
        return timetableRepository.findByDailyPlanIdWithSlots(dailyPlan.getDailyPlanId())
                .map(timetable -> TimetableResponse.from(timetable, dailyPlan))
                .orElse(null);
    }
}
