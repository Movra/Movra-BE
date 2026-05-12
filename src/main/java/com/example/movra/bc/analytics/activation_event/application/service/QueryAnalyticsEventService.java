package com.example.movra.bc.analytics.activation_event.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.dto.response.AnalyticsEventResponse;
import com.example.movra.bc.analytics.activation_event.domain.exception.InvalidAnalyticsEventException;
import com.example.movra.bc.analytics.activation_event.domain.repository.AnalyticsEventRepository;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryAnalyticsEventService {

    private final AnalyticsEventRepository analyticsEventRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<AnalyticsEventResponse> query(LocalDate from, LocalDate to, AnalyticsEventType eventType) {
        validateDateRange(from, to);

        UserId userId = currentUserQuery.currentUser().userId();
        Instant fromInstant = from.atStartOfDay(clock.getZone()).toInstant();
        Instant toExclusive = to.plusDays(1).atStartOfDay(clock.getZone()).toInstant();

        if (eventType == null) {
            return analyticsEventRepository
                    .findAllByUserIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtAsc(
                            userId,
                            fromInstant,
                            toExclusive
                    )
                    .stream()
                    .map(AnalyticsEventResponse::from)
                    .toList();
        }

        return analyticsEventRepository
                .findAllByUserIdAndEventTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtAsc(
                        userId,
                        eventType,
                        fromInstant,
                        toExclusive
                )
                .stream()
                .map(AnalyticsEventResponse::from)
                .toList();
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new InvalidAnalyticsEventException();
        }
    }
}
