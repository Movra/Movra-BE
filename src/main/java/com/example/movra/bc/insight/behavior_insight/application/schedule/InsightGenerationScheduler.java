package com.example.movra.bc.insight.behavior_insight.application.schedule;

import com.example.movra.bc.insight.behavior_insight.application.service.InsightSchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 오전 8시(KST)에 롤링 30일 분석 리포트 생성 배치를 실행한다.
 * 8시인 이유: NotificationGateway가 수면시간(22~07시) 푸시를 막으므로, 생성 직후 완료 알림이 정상 발송되도록.
 */
@Component
@RequiredArgsConstructor
public class InsightGenerationScheduler {

    private final InsightSchedulingService insightSchedulingService;

    @Scheduled(cron = "${app.insight.generation.cron:0 0 8 * * *}", zone = "${app.time.zone:Asia/Seoul}")
    public void generateDueReports() {
        insightSchedulingService.runDueReports();
    }
}
