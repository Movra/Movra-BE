package com.example.movra.bc.notification.timing.application.schedule;

import com.example.movra.bc.notification.timing.application.service.SendTimingPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimingPushScheduler {

    private final SendTimingPushService sendTimingPushService;

    @Scheduled(cron = "${app.notification.timing.cron:0 0 * * * *}", zone = "${app.time.zone:Asia/Seoul}")
    public void sendDueNotifications() {
        sendTimingPushService.sendDueNotifications();
    }
}
