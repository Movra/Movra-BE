package com.example.movra.bc.planning.daily_plan.application.service.daily_plan;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanAlreadyExistsException;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.sharedkernel.exception.DataIntegrityViolationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TodayDailyPlanProvisioner {

    private final DailyPlanRepository dailyPlanRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DailyPlan createOrLoadToday(UserId userId, LocalDate today) {
        try {
            return dailyPlanRepository.saveAndFlush(DailyPlan.create(userId, today));
        } catch (DataIntegrityViolationException e) {
            if (!DataIntegrityViolationUtils.isDuplicateKeyViolation(e)) {
                throw e;
            }
            return dailyPlanRepository.findByUserIdAndPlanDateWithTasks(userId, today)
                    .orElseThrow(DailyPlanAlreadyExistsException::new);
        }
    }
}
