package com.example.movra.bc.insight.behavior_insight.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.service.llm.InsightNarrativeGenerator;
import com.example.movra.bc.insight.behavior_insight.application.service.llm.dto.InsightNarrativeContent;
import com.example.movra.bc.insight.behavior_insight.application.service.llm.dto.InsightNarrativeRequest;
import com.example.movra.bc.insight.behavior_insight.application.service.support.AnalyticsEventReadPort;
import com.example.movra.bc.insight.behavior_insight.application.service.support.BehaviorProfileReadPort;
import com.example.movra.bc.insight.behavior_insight.application.service.support.FocusInsightReadPort;
import com.example.movra.bc.insight.behavior_insight.application.service.support.InsightMetricsCalculator;
import com.example.movra.bc.insight.behavior_insight.application.service.support.ProfileDriftDetector;
import com.example.movra.bc.insight.behavior_insight.application.service.support.ReflectionReadPort;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.AnalyticsEventView;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.BehaviorProfileView;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.FocusSessionView;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.ReflectionTextView;
import com.example.movra.bc.insight.behavior_insight.domain.InsightReport;
import com.example.movra.bc.insight.behavior_insight.domain.event.ProfileDriftDetectedEvent;
import com.example.movra.bc.insight.behavior_insight.domain.event.ProfileDriftItem;
import com.example.movra.bc.insight.behavior_insight.domain.repository.InsightReportRepository;
import com.example.movra.bc.insight.behavior_insight.domain.vo.AnalysisPeriod;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightMetrics;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightNarrative;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightReportId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * 한 사용자의 분석 기간 데이터를 read-port로 수집·집계하고(결정론적 지표),
 * 선언 선호와의 괴리를 감지하며, LLM으로 서사를 생성해 리포트를 만든다.
 * 서사 생성이 실패해도 지표는 보존하고 NARRATIVE_FAILED로 남긴다(부분 실패 허용).
 * 괴리가 감지되면 ProfileDriftDetectedEvent를 발행해 personalization이 조정 제안을 기록하게 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateInsightReportService {

    private final AnalyticsEventReadPort analyticsEventReadPort;
    private final FocusInsightReadPort focusInsightReadPort;
    private final ReflectionReadPort reflectionReadPort;
    private final BehaviorProfileReadPort behaviorProfileReadPort;
    private final InsightMetricsCalculator insightMetricsCalculator;
    private final ProfileDriftDetector profileDriftDetector;
    private final InsightNarrativeGenerator insightNarrativeGenerator;
    private final InsightReportRepository insightReportRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public InsightReportId generate(UserId userId, AnalysisPeriod period) {
        ZoneId zone = clock.getZone();
        Instant from = period.periodStart().atStartOfDay(zone).toInstant();
        Instant toExclusive = period.periodEnd().plusDays(1).atStartOfDay(zone).toInstant();

        List<AnalyticsEventView> events = analyticsEventReadPort.findEvents(userId, from, toExclusive);
        List<FocusSessionView> sessions = focusInsightReadPort.findSessions(userId, from, toExclusive);
        List<ReflectionTextView> reflections =
                reflectionReadPort.findReflectionTexts(userId, period.periodStart(), period.periodEnd());
        int reflectionCount = reflectionReadPort.countReflections(userId, period.periodStart(), period.periodEnd());
        int tinyWinCount = reflectionReadPort.countTinyWins(userId, period.periodStart(), period.periodEnd());
        BehaviorProfileView profile = behaviorProfileReadPort.findProfile(userId).orElse(null);

        InsightMetrics metrics = insightMetricsCalculator.calculate(
                events, sessions, reflectionCount, tinyWinCount, zone
        );

        List<ProfileDriftItem> driftItems = profileDriftDetector.detect(metrics, profile);
        List<String> driftMessages = driftItems.stream().map(ProfileDriftItem::message).toList();

        InsightReport report = InsightReport.ofMetrics(userId, period, metrics, clock.instant());
        applyNarrative(report, new InsightNarrativeRequest(period, metrics, reflections, profile, driftMessages));

        insightReportRepository.save(report);

        if (!driftItems.isEmpty()) {
            eventPublisher.publishEvent(new ProfileDriftDetectedEvent(userId, driftItems));
        }

        return report.getId();
    }

    private void applyNarrative(InsightReport report, InsightNarrativeRequest request) {
        try {
            InsightNarrativeContent content = insightNarrativeGenerator.generate(request);
            report.markNarrativeCompleted(toNarrative(content));
        } catch (Exception e) {
            log.error("Insight 서사 생성 실패 - 지표는 보존하고 NARRATIVE_FAILED로 표시합니다.", e);
            report.markNarrativeFailed();
        }
    }

    private InsightNarrative toNarrative(InsightNarrativeContent content) {
        return InsightNarrative.of(
                content.summary(),
                content.strengths(),
                content.improvements(),
                content.patterns(),
                content.motivation(),
                content.profileSuggestion()
        );
    }
}
