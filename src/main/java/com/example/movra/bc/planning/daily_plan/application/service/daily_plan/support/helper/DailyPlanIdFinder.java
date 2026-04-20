package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.helper;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyPlanIdFinder {

    private final DailyPlanRepository dailyPlanRepository;

    public Optional<DailyPlanId> findIdByUserAndDate(UserId userId, LocalDate date) {
        return dailyPlanRepository.findByUserIdAndPlanDate(userId, date)
                .map(DailyPlan::getDailyPlanId);
    }
}
