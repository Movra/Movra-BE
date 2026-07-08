package com.example.movra.bc.insight.behavior_insight.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.exception.InsightReportNotFoundException;
import com.example.movra.bc.insight.behavior_insight.application.service.dto.response.InsightReportResponse;
import com.example.movra.bc.insight.behavior_insight.domain.repository.InsightReportRepository;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightReportId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryInsightReportService {

    private final InsightReportRepository insightReportRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public InsightReportResponse queryLatest() {
        UserId userId = currentUserQuery.currentUser().userId();
        return insightReportRepository.findFirstByUserIdOrderByPeriod_PeriodEndDesc(userId)
                .map(InsightReportResponse::from)
                .orElseThrow(InsightReportNotFoundException::new);
    }

    /**
     * 알림 딥링크로 특정 리포트를 조회한다. 본인 소유가 아니면 존재를 노출하지 않고 NotFound로 처리한다.
     */
    @Transactional(readOnly = true)
    public InsightReportResponse queryById(UUID insightReportId) {
        UserId userId = currentUserQuery.currentUser().userId();
        return insightReportRepository.findById(InsightReportId.of(insightReportId))
                .filter(report -> report.getUserId().equals(userId))
                .map(InsightReportResponse::from)
                .orElseThrow(InsightReportNotFoundException::new);
    }
}
