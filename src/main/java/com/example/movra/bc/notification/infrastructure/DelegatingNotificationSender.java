package com.example.movra.bc.notification.infrastructure;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.sharedkernel.notification.NotificationChannelSender;
import com.example.movra.sharedkernel.notification.NotificationDeliveryException;
import com.example.movra.sharedkernel.notification.NotificationPayload;
import com.example.movra.sharedkernel.notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class DelegatingNotificationSender implements NotificationSender {

    private final List<NotificationChannelSender> channelSenders;

    @Override
    public void send(UserId targetUserId, NotificationPayload payload) {
        if (channelSenders.isEmpty()) {
            throw new NotificationDeliveryException("No notification channel sender configured.");
        }

        int successCount = 0;
        RuntimeException lastFailure = null;

        for (NotificationChannelSender channelSender : channelSenders) {
            try {
                channelSender.send(targetUserId, payload);
                successCount++;
            } catch (RuntimeException e) {
                lastFailure = e;
                log.warn("Notification channel delivery failed. channel={}, userId={}, type={}",
                        channelSender.getClass().getSimpleName(),
                        targetUserId.id(),
                        payload.type(),
                        e);
            }
        }

        if (successCount == 0) {
            throw new NotificationDeliveryException("Notification delivery failed for all channels.", lastFailure);
        }
    }
}
