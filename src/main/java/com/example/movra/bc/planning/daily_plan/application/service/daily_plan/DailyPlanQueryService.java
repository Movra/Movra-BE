package com.example.movra.bc.planning.daily_plan.application.service.daily_plan;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanAlreadyExistsException;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response.DailyPlanResponse;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyPlanQueryService {

    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public DailyPlanResponse findByPlanDate(LocalDate planDate) {
        UserId userId = currentUserQuery.currentUser().userId();

        DailyPlan dailyPlan = dailyPlanRepository.findByUserIdAndPlanDate(userId, planDate)
                .orElseThrow(DailyPlanNotFoundException::new);

        return DailyPlanResponse.from(dailyPlan);
    }

    @Transactional
    public DailyPlanResponse findOrCreateToday() {
        UserId userId = currentUserQuery.currentUser().userId();
        LocalDate today = LocalDate.now();

        DailyPlan dailyPlan = dailyPlanRepository.findByUserIdAndPlanDate(userId, today)
                .orElseGet(() -> createOrLoadToday(userId, today));

        return DailyPlanResponse.from(dailyPlan);
    }

    private DailyPlan createOrLoadToday(UserId userId, LocalDate today) {
        try {
            return dailyPlanRepository.saveAndFlush(DailyPlan.create(userId, today));
        } catch (DataIntegrityViolationException e) {
            return dailyPlanRepository.findByUserIdAndPlanDate(userId, today)
                    .orElseThrow(DailyPlanAlreadyExistsException::new);
        }
    }
}
