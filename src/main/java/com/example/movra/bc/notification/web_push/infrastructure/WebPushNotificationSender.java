package com.example.movra.bc.notification.web_push.infrastructure;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.web_push.domain.WebPushSubscription;
import com.example.movra.bc.notification.web_push.domain.repository.WebPushSubscriptionRepository;
import com.example.movra.sharedkernel.notification.NotificationChannelSender;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.web-push", name = "enabled", havingValue = "true")
public class WebPushNotificationSender implements NotificationChannelSender {

    private final WebPushSubscriptionRepository webPushSubscriptionRepository;
    private final WebPushClient webPushClient;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void send(UserId targetUserId, NotificationPayload payload) {
        List<WebPushSubscription> subscriptions =
                webPushSubscriptionRepository.findAllByUserIdAndRevokedAtIsNull(targetUserId);
        if (subscriptions.isEmpty()) {
            throw new WebPushNotificationDeliveryException("No Web Push subscription registered for user.");
        }

        String message = toMessage(payload);
        int successCount = 0;
        RuntimeException lastFailure = null;

        for (WebPushSubscription subscription : subscriptions) {
            try {
                WebPushDeliveryOutcome outcome = webPushClient.send(subscription, message);
                if (outcome == WebPushDeliveryOutcome.SENT) {
                    subscription.markActive(clock);
                    webPushSubscriptionRepository.save(subscription);
                    successCount++;
                } else if (outcome == WebPushDeliveryOutcome.SUBSCRIPTION_GONE) {
                    subscription.revoke(clock);
                    webPushSubscriptionRepository.save(subscription);
                }
            } catch (RuntimeException e) {
                lastFailure = e;
                log.warn("Web Push delivery failed. subscriptionId={}, userId={}, type={}",
                        subscription.getId().id(),
                        targetUserId.id(),
                        payload.type(),
                        e);
            }
        }

        if (successCount == 0) {
            throw new WebPushNotificationDeliveryException(
                    "Web Push delivery failed for all subscriptions.",
                    lastFailure
            );
        }
    }

    private String toMessage(NotificationPayload payload) {
        try {
            return objectMapper.writeValueAsString(
                    new WebPushMessage(payload.type().name(), payload.title(), payload.body(), payload.data())
            );
        } catch (JsonProcessingException e) {
            throw new WebPushNotificationDeliveryException("Failed to serialize Web Push payload.", e);
        }
    }

    private record WebPushMessage(
            String type,
            String title,
            String body,
            Map<String, String> data
    ) {
    }
}
