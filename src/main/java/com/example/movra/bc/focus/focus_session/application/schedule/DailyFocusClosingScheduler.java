package com.example.movra.bc.focus.focus_session.application.schedule;

import com.example.movra.bc.focus.focus_session.application.service.support.DailyFocusClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 자정 직후 전날 집중 세션을 마감해 일별 요약 스냅샷을 생성한다.
 */
@Component
@RequiredArgsConstructor
public class DailyFocusClosingScheduler {

    private final DailyFocusClosingService dailyFocusClosingService;

    @Scheduled(cron = "${app.focus.daily-close.cron:0 10 0 * * *}", zone = "${app.time.zone:Asia/Seoul}")
    public void closeYesterday() {
        dailyFocusClosingService.closeYesterday();
    }
}
