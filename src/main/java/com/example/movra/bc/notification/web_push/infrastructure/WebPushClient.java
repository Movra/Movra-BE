package com.example.movra.bc.notification.web_push.infrastructure;

import com.example.movra.bc.notification.web_push.domain.WebPushSubscription;

public interface WebPushClient {

    WebPushDeliveryOutcome send(WebPushSubscription subscription, String payload);
}
