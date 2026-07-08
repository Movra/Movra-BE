package com.example.movra.bc.insight.behavior_insight.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.exception.InsightGenerationForbiddenException;
import com.example.movra.bc.insight.behavior_insight.application.exception.InsightReportNotFoundException;
import com.example.movra.bc.insight.behavior_insight.application.service.dto.response.InsightReportResponse;
import com.example.movra.bc.insight.behavior_insight.application.service.support.InsightDueResolver;
import com.example.movra.bc.insight.behavior_insight.domain.repository.InsightReportRepository;
import com.example.movra.bc.insight.behavior_insight.domain.vo.AnalysisPeriod;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightReportId;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

/**
 * 테스트/검증용 수동 트리거. admin 계정만 호출할 수 있으며,
 * 현재 사용자의 최근 30일(또는 지정 기간) 리포트를 실제 OpenAI 호출까지 포함해 즉시 생성한다.
 * 같은 기간을 반복 검증할 수 있도록, 동일 기간의 기존 리포트가 있으면 삭제 후 재생성한다.
 * (트랜잭션을 분리하기 위해 의도적으로 @Transactional을 두지 않는다 — 삭제 커밋 후 생성.)
 *
 * 권한: tbl_users.account_id 는 UNIQUE 하므로, accountId가 ADMIN_ACCOUNT_ID와 일치하는 단일 계정만 허용한다.
 * 의도적 하드코딩 — 운영에서 admin 개념이 커지면 UserRole(Spring Security authorities) 기반으로 교체할 것.
 */
@Service
@RequiredArgsConstructor
public class TriggerInsightGenerationService {

    private static final String ADMIN_ACCOUNT_ID = "admin";

    private final GenerateInsightReportService generateInsightReportService;
    private final InsightReportRepository insightReportRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    public InsightReportResponse generateForCurrentUser(LocalDate periodStart, LocalDate periodEnd) {
        AuthenticatedUser user = currentUserQuery.currentUser();
        if (!ADMIN_ACCOUNT_ID.equals(user.accountId())) {
            throw new InsightGenerationForbiddenException();
        }

        UserId userId = user.userId();
        AnalysisPeriod period = (periodStart != null && periodEnd != null)
                ? new AnalysisPeriod(periodStart, periodEnd)
                : AnalysisPeriod.lastDays(LocalDate.now(clock), InsightDueResolver.WINDOW_DAYS);

        insightReportRepository.findByUserIdAndPeriod_PeriodStart(userId, period.periodStart())
                .ifPresent(insightReportRepository::delete);

        InsightReportId id = generateInsightReportService.generate(userId, period);

        return insightReportRepository.findById(id)
                .map(InsightReportResponse::from)
                .orElseThrow(InsightReportNotFoundException::new);
    }
}
