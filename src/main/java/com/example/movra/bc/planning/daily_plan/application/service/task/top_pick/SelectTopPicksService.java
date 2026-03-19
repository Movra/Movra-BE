package com.example.movra.bc.planning.daily_plan.application.service.task.top_pick;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.request.TopPicksRequest;
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
public class SelectTopPicksService {

    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void select(TopPicksRequest request, UUID dailyPlanId, UUID taskId){

        DailyPlan dailyPlan = dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), currentUserQuery.currentUser().userId())
                .orElseThrow(DailyPlanNotFoundException::new);

        dailyPlan.markAsTopPicked(TaskId.of(taskId), request.estimatedMinutes(), request.memo());

        dailyPlanRepository.save(dailyPlan);
    }
}
