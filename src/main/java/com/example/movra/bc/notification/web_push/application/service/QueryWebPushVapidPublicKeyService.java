package com.example.movra.bc.notification.web_push.application.service;

import com.example.movra.bc.notification.web_push.application.service.dto.response.WebPushVapidPublicKeyResponse;
import com.example.movra.config.webpush.WebPushProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryWebPushVapidPublicKeyService {

    private final WebPushProperties webPushProperties;

    @Transactional(readOnly = true)
    public WebPushVapidPublicKeyResponse query() {
        return new WebPushVapidPublicKeyResponse(webPushProperties.vapidPublicKey());
    }
}
