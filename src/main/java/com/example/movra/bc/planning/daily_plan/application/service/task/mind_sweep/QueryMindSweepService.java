package com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.response.MindSweepResponse;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.type.TaskType;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryMindSweepService {

    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public List<MindSweepResponse> queryAll(UUID dailyPlanId) {
        DailyPlan dailyPlan = dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), currentUserQuery.currentUser().userId())
                .orElseThrow(DailyPlanNotFoundException::new);

        return dailyPlan.getTasks().stream()
                .filter(task -> task.getTaskType() == TaskType.GENERAL)
                .filter(task -> !task.isTopPicked())
                .map(MindSweepResponse::from)
                .toList();
    }
}
