package com.example.movra.bc.notification.web_push.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.web_push.application.service.dto.request.WebPushSubscriptionRequest;
import com.example.movra.bc.notification.web_push.application.service.dto.response.WebPushSubscriptionResponse;
import com.example.movra.bc.notification.web_push.domain.WebPushSubscription;
import com.example.movra.bc.notification.web_push.domain.exception.InvalidWebPushSubscriptionException;
import com.example.movra.bc.notification.web_push.domain.repository.WebPushSubscriptionRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegisterWebPushSubscriptionService {

    private final WebPushSubscriptionRepository webPushSubscriptionRepository;
    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;
    private final Clock clock;

    @Transactional
    public WebPushSubscriptionResponse register(WebPushSubscriptionRequest request) {
        validateRequest(request);

        UserId userId = currentUserQuery.currentUser().userId();
        String endpointHash = sha256Hex(request.endpoint());

        WebPushSubscription subscription = webPushSubscriptionRepository.findByEndpointHash(endpointHash)
                .orElse(null);
        boolean optIn = subscription == null || !subscription.belongsTo(userId);

        if (subscription == null) {
            subscription = WebPushSubscription.register(
                    userId,
                    request.endpoint(),
                    endpointHash,
                    request.keys().p256dh(),
                    request.keys().auth(),
                    request.contentEncoding(),
                    request.userAgent(),
                    clock
            );
        } else {
            subscription.updateRegistration(
                    userId,
                    request.keys().p256dh(),
                    request.keys().auth(),
                    request.contentEncoding(),
                    request.userAgent(),
                    clock
            );
        }

        WebPushSubscription saved = webPushSubscriptionRepository.save(subscription);
        if (optIn) {
            analyticsEventRecorder.recordSafely(
                    userId,
                    AnalyticsEventType.WEB_PUSH_OPT_IN,
                    analyticsProperties(saved)
            );
        }

        return WebPushSubscriptionResponse.from(saved);
    }

    private Map<String, String> analyticsProperties(WebPushSubscription subscription) {
        Map<String, String> properties = new HashMap<>();
        properties.put("webPushSubscriptionId", subscription.getId().id().toString());
        properties.put("contentEncoding", subscription.getContentEncoding());
        if (subscription.getUserAgent() != null) {
            properties.put("userAgent", subscription.getUserAgent());
        }
        return properties;
    }

    private void validateRequest(WebPushSubscriptionRequest request) {
        if (request == null
                || request.endpoint() == null
                || request.endpoint().isBlank()
                || request.keys() == null) {
            throw new InvalidWebPushSubscriptionException();
        }
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }
}
