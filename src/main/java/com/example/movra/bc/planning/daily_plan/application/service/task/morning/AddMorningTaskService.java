package com.example.movra.bc.planning.daily_plan.application.service.task.morning;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.task.mind_sweep.dto.request.MindSweepRequest;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AddMorningTaskService {

    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void create(MindSweepRequest request, LocalDate targetDate) {
        UserId userId = currentUserQuery.currentUser().userId();

        DailyPlan dailyPlan = dailyPlanRepository.findByUserIdAndPlanDate(userId, targetDate)
                .orElseGet(() -> DailyPlan.create(userId, targetDate));

        dailyPlan.addMorningTask(request.content());
        dailyPlanRepository.save(dailyPlan);
    }
}
