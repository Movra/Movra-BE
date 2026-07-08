package com.example.movra.bc.insight.behavior_insight.domain.event;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightReportId;

/**
 * 분석 리포트가 생성되었을 때 발행. notification BC가 수신해 FCM 푸시를 전송한다(Phase 3).
 */
public record InsightReportGeneratedEvent(
        InsightReportId insightReportId,
        UserId userId
) {}
