package com.example.movra.bc.insight.behavior_insight.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.domain.repository.AnalyticsEventRepository;
import com.example.movra.bc.insight.behavior_insight.application.service.support.dto.AnalyticsEventView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsEventReadAdapter implements AnalyticsEventReadPort {

    private final AnalyticsEventRepository analyticsEventRepository;

    @Override
    public List<AnalyticsEventView> findEvents(UserId userId, Instant from, Instant toExclusive) {
        return analyticsEventRepository
                .findAllByUserIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtAsc(
                        userId,
                        from,
                        toExclusive
                )
                .stream()
                .map(event -> new AnalyticsEventView(
                        event.getEventType().name(),
                        event.getOccurredAt(),
                        event.getProperties()
                ))
                .toList();
    }

    @Override
    public List<UserId> findActiveUserIds(Instant since) {
        return analyticsEventRepository.findDistinctUserIdsSince(since);
    }

    @Override
    public Optional<LocalDate> findFirstActivityDate(UserId userId, ZoneId zone) {
        return analyticsEventRepository.findFirstByUserIdOrderByOccurredAtAsc(userId)
                .map(event -> event.getOccurredAt().atZone(zone).toLocalDate());
    }
}
