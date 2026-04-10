package com.example.movra.bc.planning.daily_plan.application.service.daily_plan;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanAlreadyExistsException;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.request.DailyPlanRequest;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyPlanCreateService {

    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void create(DailyPlanRequest dailyPlanRequest) {

        UserId userId = currentUserQuery.currentUser().userId();

        if (dailyPlanRepository.existsByUserIdAndPlanDate(userId, dailyPlanRequest.planDate())) {
            throw new DailyPlanAlreadyExistsException();
        }

        try {
            dailyPlanRepository.saveAndFlush(DailyPlan.create(userId, dailyPlanRequest.planDate()));
        } catch (DataIntegrityViolationException e) {
            throw new DailyPlanAlreadyExistsException();
        }
    }
}
