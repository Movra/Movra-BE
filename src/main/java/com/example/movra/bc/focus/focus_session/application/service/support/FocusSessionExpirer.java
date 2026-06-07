package com.example.movra.bc.focus.focus_session.application.service.support;

import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.focus.focus_session.domain.vo.FocusSessionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/**
 * 단일 집중 세션을 자동 마감 상한({@link FocusSession#MAX_SESSION_DURATION}) 시점으로 마감한다.
 * <p>
 * 세션별 독립 트랜잭션으로 처리되어 한 세션의 실패가 다른 세션 마감에 영향을 주지 않는다.
 * 멱등하게 동작하도록 진행 중·만료 여부를 다시 확인한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FocusSessionExpirer {

    private final FocusSessionRepository focusSessionRepository;
    private final AnalyticsEventRecorder analyticsEventRecorder;
    private final Clock clock;

    @Transactional
    public boolean expire(FocusSessionId focusSessionId) {
        FocusSession focusSession = focusSessionRepository.findById(focusSessionId)
                .orElse(null);

        if (focusSession == null || !focusSession.isExpiredAt(clock.instant())) {
            return false;
        }

        focusSession.autoClose();
        focusSessionRepository.save(focusSession);

        analyticsEventRecorder.recordSafely(
                focusSession.getUserId(),
                AnalyticsEventType.FOCUS_SESSION_AUTO_CLOSED,
                autoClosedProperties(focusSession)
        );
        return true;
    }

    private Map<String, String> autoClosedProperties(FocusSession focusSession) {
        Map<String, String> properties = new HashMap<>();
        properties.put("focusSessionId", focusSession.getId().id().toString());
        properties.put("startedAt", focusSession.getStartedAt().toString());
        properties.put("endedAt", focusSession.getEndedAt().toString());
        properties.put("durationSeconds", String.valueOf(focusSession.getDurationSeconds()));
        properties.put("unlimited", String.valueOf(focusSession.isUnlimited()));
        properties.put("autoClosed", "true");
        return properties;
    }
}
