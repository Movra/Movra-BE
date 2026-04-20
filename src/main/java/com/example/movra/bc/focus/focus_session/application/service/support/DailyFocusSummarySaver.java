package com.example.movra.bc.focus.focus_session.application.service.support;

import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.sharedkernel.exception.DataIntegrityViolationUtils;
import com.example.movra.sharedkernel.persistence.RequiresNewInsertExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyFocusSummarySaver {

    private final DailyFocusSummaryRepository dailyFocusSummaryRepository;
    private final RequiresNewInsertExecutor requiresNewInsertExecutor;

    public boolean save(DailyFocusSummary summary) {
        try {
            requiresNewInsertExecutor.execute(() -> dailyFocusSummaryRepository.saveAndFlush(summary));
            return true;
        } catch (DataIntegrityViolationException e) {
            if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e)) {
                return false;
            }
            throw e;
        }
    }
}
