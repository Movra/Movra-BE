package com.example.movra.bc.planning.daily_plan.application.service.query;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyPlanLookupService {

    private final DailyPlanRepository dailyPlanRepository;

    public Optional<DailyPlanId> findIdByUserAndDate(UserId userId, LocalDate date) {
        return dailyPlanRepository.findByUserIdAndPlanDate(userId, date)
                .map(plan -> plan.getDailyPlanId());
    }
}
