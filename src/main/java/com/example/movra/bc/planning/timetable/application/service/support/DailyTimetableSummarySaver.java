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

    private final DailyTimetableSummaryRepository dailyTimetableSummaryRepository;
    private final RequiresNewInsertExecutor requiresNewInsertExecutor;

    public boolean save(DailyTimetableSummary summary) {
        try {
            requiresNewInsertExecutor.execute(() -> dailyTimetableSummaryRepository.saveAndFlush(summary));
            return true;
        } catch (DataIntegrityViolationException e) {
            if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e)) {
                return false;
            }
            throw e;
        }
    }
}
