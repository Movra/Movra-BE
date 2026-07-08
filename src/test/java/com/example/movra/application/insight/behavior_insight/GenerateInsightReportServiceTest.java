package com.example.movra.application.insight.behavior_insight;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.service.GenerateInsightReportService;
import com.example.movra.bc.insight.behavior_insight.application.service.llm.InsightNarrativeGenerator;
import com.example.movra.bc.insight.behavior_insight.application.service.llm.dto.InsightNarrativeContent;
import com.example.movra.bc.insight.behavior_insight.application.service.support.AnalyticsEventReadPort;
import com.example.movra.bc.insight.behavior_insight.application.service.support.BehaviorProfileReadPort;
import com.example.movra.bc.insight.behavior_insight.application.service.support.FocusInsightReadPort;
import com.example.movra.bc.insight.behavior_insight.application.service.support.InsightMetricsCalculator;
import com.example.movra.bc.insight.behavior_insight.application.service.support.ProfileDriftDetector;
import com.example.movra.bc.insight.behavior_insight.application.service.support.ReflectionReadPort;
import com.example.movra.bc.insight.behavior_insight.domain.InsightReport;
import com.example.movra.bc.insight.behavior_insight.domain.repository.InsightReportRepository;
import com.example.movra.bc.insight.behavior_insight.domain.type.InsightReportStatus;
import com.example.movra.bc.insight.behavior_insight.domain.vo.AnalysisPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenerateInsightReportServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private AnalyticsEventReadPort analyticsEventReadPort;
    private FocusInsightReadPort focusInsightReadPort;
    private ReflectionReadPort reflectionReadPort;
    private BehaviorProfileReadPort behaviorProfileReadPort;
    private InsightNarrativeGenerator insightNarrativeGenerator;
    private InsightReportRepository insightReportRepository;
    private ApplicationEventPublisher eventPublisher;
    private GenerateInsightReportService service;

    private final UserId userId = UserId.newId();
    private final AnalysisPeriod period = AnalysisPeriod.lastDays(LocalDate.of(2026, 5, 31), 30);

    @BeforeEach
    void setUp() {
        analyticsEventReadPort = mock(AnalyticsEventReadPort.class);
        focusInsightReadPort = mock(FocusInsightReadPort.class);
        reflectionReadPort = mock(ReflectionReadPort.class);
        behaviorProfileReadPort = mock(BehaviorProfileReadPort.class);
        insightNarrativeGenerator = mock(InsightNarrativeGenerator.class);
        insightReportRepository = mock(InsightReportRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZONE);

        service = new GenerateInsightReportService(
                analyticsEventReadPort,
                focusInsightReadPort,
                reflectionReadPort,
                behaviorProfileReadPort,
                new InsightMetricsCalculator(),
                new ProfileDriftDetector(),
                insightNarrativeGenerator,
                insightReportRepository,
                eventPublisher,
                clock
        );

        when(analyticsEventReadPort.findEvents(any(), any(), any())).thenReturn(List.of());
        when(focusInsightReadPort.findSessions(any(), any(), any())).thenReturn(List.of());
        when(reflectionReadPort.findReflectionTexts(any(), any(), any())).thenReturn(List.of());
        when(reflectionReadPort.countReflections(any(), any(), any())).thenReturn(0);
        when(reflectionReadPort.countTinyWins(any(), any(), any())).thenReturn(0);
        when(behaviorProfileReadPort.findProfile(any())).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("generate_서사생성성공_COMPLETED로저장한다")
    void generate_narrativeSuccess_marksCompleted() {
        when(insightNarrativeGenerator.generate(any())).thenReturn(new InsightNarrativeContent(
                "이번 달 요약",
                List.of("강점1"),
                List.of("개선1"),
                List.of("패턴1"),
                "동기부여 메시지",
                ""
        ));

        service.generate(userId, period);

        InsightReport saved = captureSavedReport();
        assertThat(saved.getStatus()).isEqualTo(InsightReportStatus.COMPLETED);
        assertThat(saved.getNarrative()).isNotNull();
        assertThat(saved.getNarrative().summary()).isEqualTo("이번 달 요약");
        assertThat(saved.getMetrics()).isNotNull();
    }

    @Test
    @DisplayName("generate_서사생성실패_NARRATIVE_FAILED로지표는보존한다")
    void generate_narrativeFails_marksNarrativeFailedButKeepsMetrics() {
        when(insightNarrativeGenerator.generate(any()))
                .thenThrow(new RuntimeException("LLM 호출 실패"));

        service.generate(userId, period);

        InsightReport saved = captureSavedReport();
        assertThat(saved.getStatus()).isEqualTo(InsightReportStatus.NARRATIVE_FAILED);
        assertThat(saved.getNarrative()).isNull();
        assertThat(saved.getMetrics()).isNotNull();
    }

    private InsightReport captureSavedReport() {
        org.mockito.ArgumentCaptor<InsightReport> captor =
                org.mockito.ArgumentCaptor.forClass(InsightReport.class);
        org.mockito.Mockito.verify(insightReportRepository).save(captor.capture());
        return captor.getValue();
    }
}
