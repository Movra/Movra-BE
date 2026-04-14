package com.example.movra.sharedkernel.notification;

import com.example.movra.bc.account.domain.user.vo.UserId;

public interface NotificationSender {
    void send(UserId targetUserId, NotificationPayload payload);
}
