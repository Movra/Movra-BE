package com.example.movra.sharedkernel.notification;

import com.example.movra.bc.account.device_token.domain.DeviceToken;
import com.example.movra.bc.account.device_token.domain.repository.DeviceTokenRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.fcm", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class FcmNotificationSender implements NotificationSender {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public void send(UserId targetUserId, NotificationPayload payload) {
        List<DeviceToken> tokens = deviceTokenRepository.findAllByUserId(targetUserId);
        if (tokens.isEmpty()) {
            log.info("No device tokens for user {}. Skipping FCM send.", targetUserId.id());
            return;
        }

        List<String> tokenValues = tokens.stream().map(DeviceToken::getToken).toList();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokenValues)
                .setNotification(Notification.builder()
                        .setTitle(payload.title())
                        .setBody(payload.body())
                        .build())
                .putAllData(payload.data())
                .putData("type", payload.type().name())
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    SendResponse sendResponse = responses.get(i);
                    if (!sendResponse.isSuccessful()) {
                        log.warn("FCM send failed for user {} at tokenIndex {}: {}",
                                targetUserId.id(), i, sendResponse.getException().getMessage());
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast failed for user {}: {}", targetUserId.id(), e.getMessage(), e);
        }
    }
}
