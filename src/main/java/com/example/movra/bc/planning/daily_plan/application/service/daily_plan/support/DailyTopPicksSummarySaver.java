package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support;

import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import com.example.movra.sharedkernel.exception.DataIntegrityViolationUtils;
import com.example.movra.sharedkernel.persistence.RequiresNewInsertExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyTopPicksSummarySaver {

    private static final String IDEMPOTENCY_CONSTRAINT = "uk_daily_top_picks_summary_user_date";
    private static final String DAILY_PLAN_CONSTRAINT = "uk_daily_top_picks_summary_daily_plan";

    private final DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;
    private final RequiresNewInsertExecutor requiresNewInsertExecutor;

    public boolean save(DailyTopPicksSummary summary) {
        try {
            requiresNewInsertExecutor.execute(() -> dailyTopPicksSummaryRepository.saveAndFlush(summary));
            return true;
        } catch (DataIntegrityViolationException e) {
            if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e, IDEMPOTENCY_CONSTRAINT)) {
                return false;
            }
            if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e, DAILY_PLAN_CONSTRAINT)
                    && dailyTopPicksSummaryRepository.existsByUserIdAndDate(summary.getUserId(), summary.getDate())) {
                return false;
            }
            throw e;
        }
    }
}
