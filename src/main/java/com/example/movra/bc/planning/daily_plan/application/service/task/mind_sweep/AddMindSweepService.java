package com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.request.MindSweepRequest;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddMindSweepService {

    private final DailyPlanRepository dailyPlanRepository;

    @Transactional
    public void create(MindSweepRequest request, UUID dailyPlanId){
        DailyPlan dailyPlan = dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))
                .orElseThrow(DailyPlanNotFoundException::new);

        dailyPlan.addTask(request.content());

        dailyPlanRepository.save(dailyPlan);
    }
}
