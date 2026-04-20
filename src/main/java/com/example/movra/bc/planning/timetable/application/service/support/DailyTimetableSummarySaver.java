package com.example.movra.bc.planning.timetable.application.service.support;

import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.repository.DailyTimetableSummaryRepository;
import com.example.movra.sharedkernel.exception.DataIntegrityViolationUtils;
import com.example.movra.sharedkernel.persistence.RequiresNewInsertExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyTimetableSummarySaver {

    private static final String IDEMPOTENCY_CONSTRAINT = "uk_daily_timetable_summary_user_date";
    private static final String DAILY_PLAN_CONSTRAINT = "uk_daily_timetable_summary_daily_plan";

    private final DailyTimetableSummaryRepository dailyTimetableSummaryRepository;
    private final RequiresNewInsertExecutor requiresNewInsertExecutor;

    public boolean save(DailyTimetableSummary summary) {
        try {
            requiresNewInsertExecutor.execute(() -> dailyTimetableSummaryRepository.saveAndFlush(summary));
            return true;
        } catch (DataIntegrityViolationException e) {
            if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e, IDEMPOTENCY_CONSTRAINT)) {
                return false;
            }
            if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e, DAILY_PLAN_CONSTRAINT)
                    && dailyTimetableSummaryRepository.existsByUserIdAndDate(summary.getUserId(), summary.getDate())) {
                return false;
            }
            throw e;
        }
    }
}
