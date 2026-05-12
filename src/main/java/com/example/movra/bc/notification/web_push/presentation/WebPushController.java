package com.example.movra.bc.notification.web_push.presentation;

import com.example.movra.bc.notification.web_push.application.service.QueryWebPushVapidPublicKeyService;
import com.example.movra.bc.notification.web_push.application.service.RegisterWebPushSubscriptionService;
import com.example.movra.bc.notification.web_push.application.service.dto.request.WebPushSubscriptionRequest;
import com.example.movra.bc.notification.web_push.application.service.dto.response.WebPushSubscriptionResponse;
import com.example.movra.bc.notification.web_push.application.service.dto.response.WebPushVapidPublicKeyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web-push")
@RequiredArgsConstructor
public class WebPushController {

    private final QueryWebPushVapidPublicKeyService queryWebPushVapidPublicKeyService;
    private final RegisterWebPushSubscriptionService registerWebPushSubscriptionService;

    @GetMapping("/vapid-public-key")
    public WebPushVapidPublicKeyResponse queryVapidPublicKey() {
        return queryWebPushVapidPublicKeyService.query();
    }

    @PostMapping("/subscribe")
    public WebPushSubscriptionResponse subscribe(@Valid @RequestBody WebPushSubscriptionRequest request) {
        return registerWebPushSubscriptionService.register(request);
    }
}
