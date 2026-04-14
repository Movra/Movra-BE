package com.example.movra.sharedkernel.notification;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.fcm", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public void send(UserId targetUserId, NotificationPayload payload) {
        log.info("[LoggingNotificationSender] type={}, title={}, body={}, dataKeys={}",
                payload.type(), payload.title(), payload.body(), payload.data().keySet());
    }
}
