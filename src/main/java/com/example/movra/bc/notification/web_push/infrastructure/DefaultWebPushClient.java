package com.example.movra.bc.notification.web_push.infrastructure;

import com.example.movra.bc.notification.web_push.domain.WebPushSubscription;
import com.example.movra.config.webpush.WebPushProperties;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.jose4j.lang.JoseException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;

@Component
@ConditionalOnProperty(prefix = "app.web-push", name = "enabled", havingValue = "true")
public class DefaultWebPushClient implements WebPushClient {

    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final int HTTP_STATUS_GONE = 410;

    private final PushService pushService;
    private final WebPushProperties webPushProperties;

    public DefaultWebPushClient(PushService pushService, WebPushProperties webPushProperties) {
        this.pushService = pushService;
        this.webPushProperties = webPushProperties;
    }

    @Override
    public WebPushDeliveryOutcome send(WebPushSubscription subscription, String payload) {
        try {
            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dhKey(),
                    subscription.getAuthKey(),
                    payload.getBytes(StandardCharsets.UTF_8),
                    webPushProperties.ttlSeconds()
            );
            HttpResponse response = pushService.send(notification, encoding(subscription.getContentEncoding()));
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                return WebPushDeliveryOutcome.SENT;
            }
            if (statusCode == HTTP_STATUS_NOT_FOUND || statusCode == HTTP_STATUS_GONE) {
                return WebPushDeliveryOutcome.SUBSCRIPTION_GONE;
            }
            throw new WebPushNotificationDeliveryException("Web Push delivery failed. statusCode=" + statusCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebPushNotificationDeliveryException("Web Push delivery was interrupted.", e);
        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException e) {
            throw new WebPushNotificationDeliveryException("Web Push delivery failed.", e);
        }
    }

    private Encoding encoding(String contentEncoding) {
        if ("aesgcm".equalsIgnoreCase(contentEncoding)) {
            return Encoding.AESGCM;
        }
        return Encoding.AES128GCM;
    }
}
