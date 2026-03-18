package com.example.movra.bc.planning.daily_plan.application.service.task.top_pick;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.response.TopPicksResponse;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryTopPicksService {

    private final DailyPlanRepository dailyPlanRepository;

    @Transactional(readOnly = true)
    public List<TopPicksResponse> queryAll(UUID dailyPlanId) {
        DailyPlan dailyPlan = dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))
                .orElseThrow(DailyPlanNotFoundException::new);

        return dailyPlan.getTasks().stream()
                .filter(Task::isTopPicked)
                .map(TopPicksResponse::from)
                .toList();
    }
}
