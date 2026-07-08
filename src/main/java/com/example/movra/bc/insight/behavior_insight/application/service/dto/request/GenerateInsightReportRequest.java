package com.example.movra.bc.insight.behavior_insight.application.service.dto.request;

import java.time.LocalDate;

/**
 * 수동 트리거 요청. 두 값이 모두 있으면 해당 기간, 없으면 최근 30일로 생성한다.
 */
public record GenerateInsightReportRequest(
        LocalDate periodStart,
        LocalDate periodEnd
) {}
