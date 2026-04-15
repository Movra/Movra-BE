package com.example.movra.bc.planning.daily_plan.application.service.daily_plan;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import com.example.movra.bc.planning.daily_plan.domain.type.ClosedBy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CloseDailyTopPicksService {

    private final DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final Clock clock;

    @Transactional
    public void close(UserId userId, LocalDate date, ClosedBy closedBy) {
        if (dailyTopPicksSummaryRepository.existsByUserIdAndDate(userId, date)) {
            return;
        }

        int totalCount = 0;
        int completedCount = 0;

        DailyPlan plan = dailyPlanRepository.findByUserIdAndPlanDate(userId, date).orElse(null);
        if (plan != null) {
            List<Task> topPicks = plan.getTasks().stream()
                    .filter(Task::isTopPicked)
                    .toList();
            totalCount = topPicks.size();
            completedCount = (int) topPicks.stream().filter(Task::isCompleted).count();
        }

        DailyTopPicksSummary summary = DailyTopPicksSummary.close(
                userId, date, totalCount, completedCount, closedBy, clock
        );
        dailyTopPicksSummaryRepository.save(summary);
    }
}
