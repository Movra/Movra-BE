package com.example.movra.application.notification.timing;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.notification.application.service.NotificationDeliveryResult;
import com.example.movra.bc.notification.application.service.NotificationGateway;
import com.example.movra.bc.notification.timing.application.service.SendTimingPushService;
import com.example.movra.bc.statistics.focus_statistics.application.service.RecommendFocusTimingService;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimingRecommendationResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimingRecommendationResponse.RecommendedHour;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SendTimingPushServiceTest {

    @Mock
    private FocusSessionRepository focusSessionRepository;

    @Mock
    private RecommendFocusTimingService recommendFocusTimingService;

    @Mock
    private NotificationGateway notificationGateway;

    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    // 2026-05-04 Mon 16:00 KST → currentHour=16
    private final Clock clock = Clock.fixed(Instant.parse("2026-05-04T07:00:00Z"), zoneId);
    private final UserId userId = UserId.newId();
    private SendTimingPushService service;

    @BeforeEach
    void setUp() {
        service = new SendTimingPushService(
                focusSessionRepository,
                recommendFocusTimingService,
                notificationGateway,
                clock
        );
    }

    @Test
    @DisplayName("sendDueNotifications sends a TIMING push when current hour matches recommended hour")
    void sendDueNotifications_matchingHour_sends() {
        given(focusSessionRepository.findDistinctUserIdsOverlappingPeriod(any(), any()))
                .willReturn(List.of(userId));
        given(recommendFocusTimingService.recommendFor(eq(userId), any(LocalDate.class), any()))
                .willReturn(recommendation(true, 14, 16));
        given(notificationGateway.sendSafely(eq(userId), any())).willReturn(NotificationDeliveryResult.SENT);

        int sent = service.sendDueNotifications();

        assertThat(sent).isEqualTo(1);
        ArgumentCaptor<NotificationPayload> captor = ArgumentCaptor.forClass(NotificationPayload.class);
        then(notificationGateway).should().sendSafely(eq(userId), captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(NotificationType.TIMING);
        assertThat(captor.getValue().data().get("recommendedHour")).isEqualTo("16");
    }

    @Test
    @DisplayName("sendDueNotifications skips when current hour does not match recommendation")
    void sendDueNotifications_nonMatchingHour_skips() {
        given(focusSessionRepository.findDistinctUserIdsOverlappingPeriod(any(), any()))
                .willReturn(List.of(userId));
        given(recommendFocusTimingService.recommendFor(eq(userId), any(LocalDate.class), any()))
                .willReturn(recommendation(true, 9, 21));

        int sent = service.sendDueNotifications();

        assertThat(sent).isZero();
        then(notificationGateway).should(never()).sendSafely(any(), any());
    }

    @Test
    @DisplayName("sendDueNotifications skips users with fallback (non data-based) recommendation")
    void sendDueNotifications_fallbackRecommendation_skips() {
        given(focusSessionRepository.findDistinctUserIdsOverlappingPeriod(any(), any()))
                .willReturn(List.of(userId));
        given(recommendFocusTimingService.recommendFor(eq(userId), any(LocalDate.class), any()))
                .willReturn(recommendation(false, 16));

        int sent = service.sendDueNotifications();

        assertThat(sent).isZero();
        then(notificationGateway).should(never()).sendSafely(any(), any());
    }

    private FocusTimingRecommendationResponse recommendation(boolean basedOnData, int... hours) {
        return FocusTimingRecommendationResponse.builder()
                .targetDate(LocalDate.now(clock))
                .queriedAt(clock.instant())
                .recommendedHours(java.util.Arrays.stream(hours)
                        .mapToObj(h -> new RecommendedHour(h, 1200L))
                        .toList())
                .reason("test")
                .basedOnData(basedOnData)
                .build();
    }
}
