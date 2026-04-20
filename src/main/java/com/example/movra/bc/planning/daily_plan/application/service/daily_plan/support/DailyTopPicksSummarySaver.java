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

    private final DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;
    private final RequiresNewInsertExecutor requiresNewInsertExecutor;

    public boolean save(DailyTopPicksSummary summary) {
        try {
            requiresNewInsertExecutor.execute(() -> dailyTopPicksSummaryRepository.saveAndFlush(summary));
            return true;
        } catch (DataIntegrityViolationException e) {
            if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e)) {
                return false;
            }
            throw e;
        }
    }
}
