package com.example.movra.bc.notification.infrastructure;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.account.user.infrastructure.user.device.DeviceToken;
import com.example.movra.bc.account.user.infrastructure.user.device.repository.DeviceTokenRepository;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationSender;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.fcm", name = "enabled", havingValue = "true")
public class FcmNotificationSender implements NotificationSender {

    private static final String NOTIFICATION_TYPE_DATA_KEY = "type";

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public void send(UserId targetUserId, NotificationPayload payload) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByUserId(targetUserId);
        if (deviceTokens.isEmpty()) {
            throw new FcmNotificationDeliveryException("No device token registered for user.");
        }

        int successCount = 0;
        FirebaseMessagingException lastFailure = null;

        for (DeviceToken deviceToken : deviceTokens) {
            try {
                firebaseMessaging.send(toMessage(deviceToken, payload));
                successCount++;
            } catch (FirebaseMessagingException e) {
                lastFailure = e;
                handleFailure(deviceToken, payload, e);
            }
        }

        if (successCount == 0) {
            throw new FcmNotificationDeliveryException("FCM notification delivery failed for all device tokens.", lastFailure);
        }
    }

    private Message toMessage(DeviceToken deviceToken, NotificationPayload payload) {
        Message.Builder builder = Message.builder()
                .setToken(deviceToken.getToken())
                .putAllData(payload.data())
                .putData(NOTIFICATION_TYPE_DATA_KEY, payload.type().name());

        Notification notification = toNotification(payload);
        if (notification != null) {
            builder.setNotification(notification);
        }

        return builder.build();
    }

    private Notification toNotification(NotificationPayload payload) {
        boolean hasTitle = StringUtils.hasText(payload.title());
        boolean hasBody = StringUtils.hasText(payload.body());

        if (!hasTitle && !hasBody) {
            return null;
        }

        Notification.Builder builder = Notification.builder();
        if (hasTitle) {
            builder.setTitle(payload.title());
        }
        if (hasBody) {
            builder.setBody(payload.body());
        }
        return builder.build();
    }

    private void handleFailure(DeviceToken deviceToken, NotificationPayload payload, FirebaseMessagingException exception) {
        log.warn("FCM notification delivery failed. tokenId={}, type={}, errorCode={}, messagingErrorCode={}",
                deviceToken.getId().id(),
                payload.type(),
                exception.getErrorCode(),
                exception.getMessagingErrorCode());

        if (exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
            deviceTokenRepository.deleteByToken(deviceToken.getToken());
        }
    }
}
