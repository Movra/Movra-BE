package com.example.movra.bc.focus.focus_session.application.service.support;

import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.focus.focus_session.domain.vo.FocusSessionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * stop이 호출되지 않아 상한({@link FocusSession#MAX_SESSION_DURATION})을 넘긴 진행 중 세션을
 * 일괄 자동 마감한다. 무제한 타이머뿐 아니라 프리셋 세션도 동일하게 보호한다.
 * <p>
 * 세션별 마감은 {@link FocusSessionExpirer#expire}가 독립 트랜잭션으로 멱등하게 처리하므로
 * 한 세션이 실패해도 전체 배치를 중단하지 않고 계속 진행한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FocusSessionAutoCloseService {

    private final FocusSessionRepository focusSessionRepository;
    private final FocusSessionExpirer focusSessionExpirer;
    private final Clock clock;

    public int closeExpiredSessions() {
        Instant threshold = clock.instant().minus(FocusSession.MAX_SESSION_DURATION);
        List<FocusSessionId> expiredIds = focusSessionRepository.findIdsInProgressStartedBefore(threshold);
        log.info("[FocusSessionAutoClose] start threshold={}, candidates={}", threshold, expiredIds.size());

        int closedCount = 0;
        for (FocusSessionId focusSessionId : expiredIds) {
            try {
                if (focusSessionExpirer.expire(focusSessionId)) {
                    closedCount++;
                }
            } catch (Exception e) {
                log.error("[FocusSessionAutoClose] failed focusSessionId={}", focusSessionId.id(), e);
            }
        }

        log.info("[FocusSessionAutoClose] done closed={}/{}", closedCount, expiredIds.size());
        return closedCount;
    }
}
