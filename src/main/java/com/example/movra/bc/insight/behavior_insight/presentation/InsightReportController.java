package com.example.movra.bc.insight.behavior_insight.presentation;

import com.example.movra.bc.insight.behavior_insight.application.service.QueryInsightReportService;
import com.example.movra.bc.insight.behavior_insight.application.service.TriggerInsightGenerationService;
import com.example.movra.bc.insight.behavior_insight.application.service.dto.request.GenerateInsightReportRequest;
import com.example.movra.bc.insight.behavior_insight.application.service.dto.response.InsightReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/insights")
@RequiredArgsConstructor
public class InsightReportController {

    private final QueryInsightReportService queryInsightReportService;
    private final TriggerInsightGenerationService triggerInsightGenerationService;

    @GetMapping("/latest")
    public InsightReportResponse queryLatest() {
        return queryInsightReportService.queryLatest();
    }

    @GetMapping("/{insightReportId}")
    public InsightReportResponse queryById(@PathVariable UUID insightReportId) {
        return queryInsightReportService.queryById(insightReportId);
    }

    /**
     * (admin 전용) 현재 사용자의 리포트를 즉시 생성한다. 실제 OpenAI 호출까지 포함되며, 결과를 그대로 반환한다.
     */
    @PostMapping("/generate")
    public InsightReportResponse generate(@RequestBody(required = false) GenerateInsightReportRequest request) {
        LocalDate periodStart = request == null ? null : request.periodStart();
        LocalDate periodEnd = request == null ? null : request.periodEnd();
        return triggerInsightGenerationService.generateForCurrentUser(periodStart, periodEnd);
    }
}
