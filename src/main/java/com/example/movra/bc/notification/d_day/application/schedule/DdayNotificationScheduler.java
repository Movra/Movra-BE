package com.example.movra.bc.notification.d_day.application.schedule;

import com.example.movra.bc.notification.d_day.application.service.SendDdayNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DdayNotificationScheduler {

    private final SendDdayNotificationService sendDdayNotificationService;

    @Scheduled(cron = "${app.notification.d-day.cron:0 0 16 * * *}", zone = "${app.time.zone:Asia/Seoul}")
    public void sendDueNotifications() {
        sendDdayNotificationService.sendDueNotifications();
    }
}
