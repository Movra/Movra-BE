package com.example.movra.bc.planning.daily_plan.application.service.task.top_pick;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeselectTopPicksService {

    private final DailyPlanRepository dailyPlanRepository;

    @Transactional
    public void deselect(UUID dailyPlanId, UUID taskId){
        DailyPlan dailyPlan = dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))
                .orElseThrow(DailyPlanNotFoundException::new);

        dailyPlan.unmarkTopPicked(TaskId.of(taskId));

        dailyPlanRepository.save(dailyPlan);
    }
}
