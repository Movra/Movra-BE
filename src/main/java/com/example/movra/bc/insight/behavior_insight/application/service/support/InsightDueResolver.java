package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.domain.InsightReport;
import com.example.movra.bc.insight.behavior_insight.domain.repository.InsightReportRepository;
import com.example.movra.bc.insight.behavior_insight.domain.vo.AnalysisPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 롤링 30일 윈도우의 "다음 생성 대상" 기간을 계산한다.
 * - 최초: 첫 활동일(firstActivityDate)을 앵커로 [앵커, 앵커+29] 윈도우.
 * - 이후: 직전 리포트 종료일+1을 시작으로 다음 30일 윈도우.
 * 윈도우가 완전히 지난 경우(today가 windowEnd 이후)에만 생성 대상으로 본다.
 */
@Component
@RequiredArgsConstructor
public class InsightDueResolver {

    public static final int WINDOW_DAYS = 30;

    private final InsightReportRepository insightReportRepository;

    @Transactional(readOnly = true)
    public Optional<AnalysisPeriod> resolveDueWindow(UserId userId, LocalDate firstActivityDate, LocalDate today) {
        LocalDate windowStart = insightReportRepository.findFirstByUserIdOrderByPeriod_PeriodEndDesc(userId)
                .map(InsightReport::getPeriod)
                .map(period -> period.periodEnd().plusDays(1))
                .orElse(firstActivityDate);

        if (windowStart == null) {
            return Optional.empty();
        }

        LocalDate windowEnd = windowStart.plusDays(WINDOW_DAYS - 1L);
        if (today.isAfter(windowEnd)) {
            return Optional.of(new AnalysisPeriod(windowStart, windowEnd));
        }
        return Optional.empty();
    }
}
