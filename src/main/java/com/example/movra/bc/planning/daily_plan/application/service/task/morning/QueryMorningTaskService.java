package com.example.movra.bc.planning.daily_plan.application.service.task.morning;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.response.MindSweepResponse;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryMorningTaskService {

    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public List<MindSweepResponse> queryAll(LocalDate targetDate) {
        UserId userId = currentUserQuery.currentUser().userId();

        DailyPlan dailyPlan = dailyPlanRepository.findByUserIdAndPlanDate(userId, targetDate)
                .orElseThrow(DailyPlanNotFoundException::new);

        return dailyPlan.getMorningTasks().stream()
                .map(MindSweepResponse::from)
                .toList();
    }
}
