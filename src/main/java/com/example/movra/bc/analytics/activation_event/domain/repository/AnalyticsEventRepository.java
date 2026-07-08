package com.example.movra.bc.analytics.activation_event.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.domain.AnalyticsEvent;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.analytics.activation_event.domain.vo.AnalyticsEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, AnalyticsEventId> {

    Optional<AnalyticsEvent> findFirstByUserIdOrderByOccurredAtAsc(UserId userId);

    @Query("select distinct e.userId from AnalyticsEvent e where e.occurredAt >= :since")
    List<UserId> findDistinctUserIdsSince(@Param("since") Instant since);

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
