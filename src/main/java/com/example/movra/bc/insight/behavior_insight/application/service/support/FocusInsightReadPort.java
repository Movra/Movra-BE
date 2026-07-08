package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.FocusSessionView;

import java.time.Instant;
import java.util.List;

/**
 * 집중 세션 조회 포트(ACL). statistics BC의 기존 read-port에 위임한다.
 */
public interface FocusInsightReadPort {

    List<FocusSessionView> findSessions(UserId userId, Instant from, Instant toExclusive);
}
