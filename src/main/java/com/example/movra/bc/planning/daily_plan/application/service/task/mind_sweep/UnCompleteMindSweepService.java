package com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnCompleteMindSweepService {

    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void unComplete(UUID dailyPlanId, UUID taskId){
        DailyPlan dailyPlan = dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), currentUserQuery.currentUser().userId())
                .orElseThrow(DailyPlanNotFoundException::new);

        dailyPlan.unCompleteTask(TaskId.of(taskId));

        dailyPlanRepository.save(dailyPlan);
    }
}
