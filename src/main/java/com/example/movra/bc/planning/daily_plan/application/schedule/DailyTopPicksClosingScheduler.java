package com.example.movra.bc.planning.daily_plan.application.schedule;

import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.DailyTopPicksClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 자정 직후 전날 Top Picks를 마감해 일별 요약 스냅샷을 생성한다.
 */
@Component
@RequiredArgsConstructor
public class DailyTopPicksClosingScheduler {

    private final DailyTopPicksClosingService dailyTopPicksClosingService;

    @Scheduled(cron = "${app.planning.top-picks.daily-close.cron:0 15 0 * * *}", zone = "${app.time.zone:Asia/Seoul}")
    public void closeYesterday() {
        dailyTopPicksClosingService.closeYesterday();
    }
}
