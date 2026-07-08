package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.FocusSessionView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsReadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * statistics BC의 기존 FocusStatisticsReadPort에 위임해 집중 세션을 조회한다.
 */
@Component
@RequiredArgsConstructor
public class FocusInsightReadAdapter implements FocusInsightReadPort {

    private final FocusStatisticsReadPort focusStatisticsReadPort;

    @Override
    public List<FocusSessionView> findSessions(UserId userId, Instant from, Instant toExclusive) {
        return focusStatisticsReadPort.findSessionsInRange(userId, from, toExclusive).stream()
                .map(session -> new FocusSessionView(session.startedAt(), session.endedAt()))
                .toList();
    }
}
