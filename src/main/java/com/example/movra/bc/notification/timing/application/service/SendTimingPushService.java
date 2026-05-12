package com.example.movra.bc.notification.timing.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.statistics.focus_statistics.application.service.RecommendFocusTimingService;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimingRecommendationResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimingRecommendationResponse.RecommendedHour;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendTimingPushService {

    private static final int LOOKBACK_DAYS = 14;

    private final FocusSessionRepository focusSessionRepository;
    private final RecommendFocusTimingService recommendFocusTimingService;
    private final NotificationGateway notificationGateway;
    private final Clock clock;

    @Transactional
    public int sendDueNotifications() {
        Instant now = clock.instant();
        ZoneId zoneId = clock.getZone();
        LocalDateTime nowLocal = now.atZone(zoneId).toLocalDateTime();
        int currentHour = nowLocal.getHour();
        LocalDate today = nowLocal.toLocalDate();

        Instant lookbackStart = today.minusDays(LOOKBACK_DAYS).atStartOfDay(zoneId).toInstant();
        List<UserId> activeUserIds =
                focusSessionRepository.findDistinctUserIdsOverlappingPeriod(lookbackStart, now);

        int sentCount = 0;
        for (UserId userId : activeUserIds) {
            try {
                FocusTimingRecommendationResponse recommendation =
                        recommendFocusTimingService.recommendFor(userId, today, now);

                if (!recommendation.basedOnData()) {
                    continue;
                }

                boolean matchesRecommendedHour = recommendation.recommendedHours().stream()
                        .map(RecommendedHour::hourOfDay)
                        .anyMatch(hour -> hour == currentHour);
                if (!matchesRecommendedHour) {
                    continue;
                }

                if (notificationGateway.sendSafely(userId, payload(currentHour))
                        == com.example.movra.bc.notification.application.service.NotificationDeliveryResult.SENT) {
                    sentCount++;
                }
            } catch (RuntimeException e) {
                log.warn("Timing push evaluation failed. userId={}", userId.id(), e);
            }
        }

        return sentCount;
    }

    private NotificationPayload payload(int currentHour) {
        return NotificationPayload.of(
                NotificationType.TIMING,
                "지금이 너의 황금 시간대",
                currentHour + "시 — 평소 가장 잘 집중되는 시간이에요. 5분만 시작해볼까요?",
                Map.of("recommendedHour", String.valueOf(currentHour))
        );
    }
}
