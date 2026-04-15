package com.example.movra.bc.day_closing.application.scheduler;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.day_closing.application.service.ClosedBy;
import com.example.movra.bc.day_closing.application.service.DayClosingOrchestrator;
import com.example.movra.bc.focus.focus_session.application.service.query.FocusDailySummaryQueryService;
import com.example.movra.bc.planning.daily_plan.application.service.query.DailyTopPicksSummaryQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyClosingScheduler {

    private final DayClosingOrchestrator dayClosingOrchestrator;
    private final FocusDailySummaryQueryService focusDailySummaryQueryService;
    private final DailyTopPicksSummaryQueryService dailyTopPicksSummaryQueryService;
    private final Clock clock;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void runDaily() {
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);
        runFor(yesterday);
    }

    public void runFor(LocalDate date) {
        Set<UserId> activeUsers = new HashSet<>();
        activeUsers.addAll(focusDailySummaryQueryService.findActiveUserIds(date));
        activeUsers.addAll(dailyTopPicksSummaryQueryService.findActiveUserIds(date));

        log.info("DailyClosingScheduler running for date={}, userCount={}", date, activeUsers.size());

        for (UserId userId : activeUsers) {
            try {
                dayClosingOrchestrator.closeUserDay(userId, date, ClosedBy.SCHEDULER);
            } catch (RuntimeException e) {
                log.error("Scheduled day closing failed for user={}, date={}: {}",
                        userId.id(), date, e.getMessage(), e);
            }
        }
    }
}
