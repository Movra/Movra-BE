package com.example.movra.bc.planning.timetable.application.schedule;

import com.example.movra.bc.planning.timetable.application.service.support.DailyTimetableClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 자정 직후 전날 타임테이블을 마감해 일별 요약 스냅샷을 생성한다.
 */
@Component
@RequiredArgsConstructor
public class DailyTimetableClosingScheduler {

    private final DailyTimetableClosingService dailyTimetableClosingService;

    @Scheduled(cron = "${app.planning.timetable.daily-close.cron:0 20 0 * * *}", zone = "${app.time.zone:Asia/Seoul}")
    public void closeYesterday() {
        dailyTimetableClosingService.closeYesterday();
    }
}
