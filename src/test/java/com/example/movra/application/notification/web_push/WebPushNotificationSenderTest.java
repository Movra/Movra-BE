package com.example.movra.application.notification.web_push;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.web_push.domain.WebPushSubscription;
import com.example.movra.bc.notification.web_push.domain.repository.WebPushSubscriptionRepository;
import com.example.movra.bc.notification.web_push.infrastructure.WebPushClient;
import com.example.movra.bc.notification.web_push.infrastructure.WebPushDeliveryOutcome;
import com.example.movra.bc.notification.web_push.infrastructure.WebPushNotificationDeliveryException;
import com.example.movra.bc.notification.web_push.infrastructure.WebPushNotificationSender;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WebPushNotificationSenderTest {

    @Mock
    private WebPushSubscriptionRepository webPushSubscriptionRepository;

    @Mock
    private WebPushClient webPushClient;

    private final UserId userId = UserId.newId();
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-30T07:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebPushNotificationSender sender;

    private final NotificationPayload payload = NotificationPayload.of(
            NotificationType.D_DAY,
            "D-7",
            "Exam is in 7 days.",
            Map.of("examScheduleId", "exam-schedule-id")
    );

    @BeforeEach
    void setUp() {
        sender = new WebPushNotificationSender(
                webPushSubscriptionRepository,
                webPushClient,
                objectMapper,
                clock
        );
    }

    @Test
    @DisplayName("send sends serialized payload to active web push subscription and stamps lastActiveAt")
    void send_withSubscription_sendsSerializedPayload() throws Exception {
        WebPushSubscription subscription = subscription("1", "a");
        given(webPushSubscriptionRepository.findAllByUserIdAndRevokedAtIsNull(userId))
                .willReturn(List.of(subscription));
        given(webPushClient.send(eq(subscription), anyString())).willReturn(WebPushDeliveryOutcome.SENT);

        sender.send(userId, payload);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        then(webPushClient).should().send(eq(subscription), payloadCaptor.capture());
        JsonNode json = objectMapper.readTree(payloadCaptor.getValue());
        assertThat(json.path("type").asText()).isEqualTo("D_DAY");
        assertThat(json.path("title").asText()).isEqualTo("D-7");
        assertThat(json.path("body").asText()).isEqualTo("Exam is in 7 days.");
        assertThat(json.path("data").path("examScheduleId").asText()).isEqualTo("exam-schedule-id");
        assertThat(subscription.getLastActiveAt()).isEqualTo(clock.instant());
        assertThat(subscription.isRevoked()).isFalse();
        then(webPushSubscriptionRepository).should().save(subscription);
    }

    @Test
    @DisplayName("send revokes gone subscription and keeps active one")
    void send_expiredAndSentSubscriptions_revokesExpiredSubscription() {
        WebPushSubscription gone = subscription("expired", "b");
        WebPushSubscription active = subscription("active", "c");
        given(webPushSubscriptionRepository.findAllByUserIdAndRevokedAtIsNull(userId))
                .willReturn(List.of(gone, active));
        given(webPushClient.send(eq(gone), anyString())).willReturn(WebPushDeliveryOutcome.SUBSCRIPTION_GONE);
        given(webPushClient.send(eq(active), anyString())).willReturn(WebPushDeliveryOutcome.SENT);

        sender.send(userId, payload);

        assertThat(gone.isRevoked()).isTrue();
        assertThat(gone.getRevokedAt()).isEqualTo(clock.instant());
        assertThat(active.isRevoked()).isFalse();
        assertThat(active.getLastActiveAt()).isEqualTo(clock.instant());
        then(webPushSubscriptionRepository).should().save(gone);
        then(webPushSubscriptionRepository).should().save(active);
        then(webPushSubscriptionRepository).should(never()).delete(gone);
    }

    @Test
    @DisplayName("send throws when target user has no active web push subscription")
    void send_noSubscription_throwsException() {
        given(webPushSubscriptionRepository.findAllByUserIdAndRevokedAtIsNull(userId)).willReturn(List.of());

        assertThatThrownBy(() -> sender.send(userId, payload))
                .isInstanceOf(WebPushNotificationDeliveryException.class)
                .hasMessage("No Web Push subscription registered for user.");
    }

    @Test
    @DisplayName("send throws when every subscription is gone and revokes them all")
    void send_allSubscriptionsGone_throwsException() {
        WebPushSubscription gone = subscription("expired", "d");
        given(webPushSubscriptionRepository.findAllByUserIdAndRevokedAtIsNull(userId))
                .willReturn(List.of(gone));
        given(webPushClient.send(eq(gone), anyString())).willReturn(WebPushDeliveryOutcome.SUBSCRIPTION_GONE);

        assertThatThrownBy(() -> sender.send(userId, payload))
                .isInstanceOf(WebPushNotificationDeliveryException.class)
                .hasMessage("Web Push delivery failed for all subscriptions.");
        assertThat(gone.isRevoked()).isTrue();
        then(webPushSubscriptionRepository).should().save(gone);
    }

    private WebPushSubscription subscription(String endpointSuffix, String hashSeed) {
        return WebPushSubscription.register(
                userId,
                "https://push.example/subscription/" + endpointSuffix,
                hashSeed.repeat(64),
                "p256dh-key",
                "auth-key",
                "aes128gcm",
                "Chrome",
                clock
        );
    }
}
