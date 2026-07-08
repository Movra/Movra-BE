package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.AnalyticsEventView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * analytics BC 이벤트 스트림 조회 포트(ACL). 분석 입력의 백본.
 */
public interface AnalyticsEventReadPort {

    List<AnalyticsEventView> findEvents(UserId userId, Instant from, Instant toExclusive);

    /**
     * since 이후 활동(이벤트)이 있는 사용자 id 목록. 스케줄러 대상(휴면 사용자 제외) 선별에 사용.
     */
    List<UserId> findActiveUserIds(Instant since);

    /**
     * 사용자의 최초 이벤트 발생일(가입/첫 활동 ≈ 롤링 윈도우 앵커). 활동이 없으면 empty.
     */
    Optional<LocalDate> findFirstActivityDate(UserId userId, ZoneId zone);
}
