package com.example.movra.bc.analytics.activation_event.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.domain.AnalyticsEvent;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.analytics.activation_event.domain.vo.AnalyticsEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, AnalyticsEventId> {

    List<AnalyticsEvent> findAllByUserIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtAsc(
            UserId userId,
            Instant from,
            Instant to
    );

    List<AnalyticsEvent> findAllByUserIdAndEventTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtAsc(
            UserId userId,
            AnalyticsEventType eventType,
            Instant from,
            Instant to
    );
}
