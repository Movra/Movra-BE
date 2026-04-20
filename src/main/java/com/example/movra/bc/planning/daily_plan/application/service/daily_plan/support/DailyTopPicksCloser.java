package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;


@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTopPicksCloser {

    private final DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final DailyTopPicksSummarySaver dailyTopPicksSummarySaver;
    private final Clock clock;

    @Transactional(readOnly = true)
    public void close(UserId userId, LocalDate date) {
        if (dailyTopPicksSummaryRepository.existsByUserIdAndDate(userId, date)) {
            return;
        }

        DailyPlan dailyPlan = dailyPlanRepository.findByUserIdAndPlanDate(userId, date)
                .orElse(null);

        if (dailyPlan == null) {
            log.debug("No daily plan for user={}, date={}; skipping DailyTopPicksSummary close", userId.id(), date);
            return;
        }

        DailyTopPicksSummary summary = DailyTopPicksSummary.close(dailyPlan, clock);

        boolean saved = dailyTopPicksSummarySaver.save(summary);
        if (!saved) {
            log.debug("DailyTopPicksSummary already exists for user={}, date={}", userId.id(), date);
        }
    }
}
