package com.example.movra.bc.planning.timetable.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.repository.DailyTimetableSummaryRepository;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTimetableCloser {

    private final DailyTimetableSummaryRepository dailyTimetableSummaryRepository;
    private final TimetableRepository timetableRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final DailyTimetableSummarySaver dailyTimetableSummarySaver;
    private final Clock clock;

    @Transactional(readOnly = true)
    public void close(UserId userId, LocalDate date) {
        if (dailyTimetableSummaryRepository.existsByUserIdAndDate(userId, date)) {
            return;
        }

        DailyPlan dailyPlan = dailyPlanRepository.findByUserIdAndPlanDate(userId, date)
                .orElse(null);

        if (dailyPlan == null) {
            log.debug("No daily plan for user={}, date={}; skipping DailyTimetableSummary close", userId.id(), date);
            return;
        }

        Timetable timetable = timetableRepository.findByDailyPlanId(dailyPlan.getDailyPlanId())
                .orElse(null);

        if (timetable == null) {
            log.debug("No timetable for user={}, date={}; skipping DailyTimetableSummary close", userId.id(), date);
            return;
        }

        DailyTimetableSummary summary = DailyTimetableSummary.close(dailyPlan, timetable, clock);

        boolean saved = dailyTimetableSummarySaver.save(summary);
        if (!saved) {
            log.debug("DailyTimetableSummary already exists for user={}, date={}", userId.id(), date);
        }
    }
}
