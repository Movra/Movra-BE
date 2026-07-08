package com.example.movra.bc.insight.behavior_insight.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.service.support.AnalyticsEventReadPort;
import com.example.movra.bc.insight.behavior_insight.application.service.support.InsightDueResolver;
import com.example.movra.bc.insight.behavior_insight.domain.vo.AnalysisPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * 롤링 30일 주기 배치. 최근 활동(30일 내) 사용자 중 다음 윈도우가 도래한 사용자에 대해 리포트를 생성한다.
 * 사용자별 생성은 각자 독립 트랜잭션(GenerateInsightReportService)이며, 한 명의 실패가 배치 전체를 멈추지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsightSchedulingService {

    private final AnalyticsEventReadPort analyticsEventReadPort;
    private final InsightDueResolver insightDueResolver;
    private final GenerateInsightReportService generateInsightReportService;
    private final Clock clock;

    public void runDueReports() {
        LocalDate today = LocalDate.now(clock);
        ZoneId zone = clock.getZone();
        Instant activeSince = clock.instant().minus(Duration.ofDays(InsightDueResolver.WINDOW_DAYS));

        List<UserId> candidates = analyticsEventReadPort.findActiveUserIds(activeSince);
        log.info("Insight 스케줄러 시작 - 후보 사용자 수={}", candidates.size());

        int generated = 0;
        for (UserId userId : candidates) {
            try {
                LocalDate firstActivity = analyticsEventReadPort.findFirstActivityDate(userId, zone).orElse(null);
                Optional<AnalysisPeriod> dueWindow = insightDueResolver.resolveDueWindow(userId, firstActivity, today);
                if (dueWindow.isPresent()) {
                    generateInsightReportService.generate(userId, dueWindow.get());
                    generated++;
                }
            } catch (Exception e) {
                log.error("Insight 생성 실패 - userId={}", userId.id(), e);
            }
        }

        log.info("Insight 스케줄러 종료 - 생성 건수={}", generated);
    }
}
