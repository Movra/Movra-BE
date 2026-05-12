package com.example.movra.bc.notification.presentation;

import com.example.movra.bc.notification.application.service.QueryNotificationPreferenceService;
import com.example.movra.bc.notification.application.service.UpdateNotificationPreferenceService;
import com.example.movra.bc.notification.application.service.dto.request.NotificationPreferenceRequest;
import com.example.movra.bc.notification.application.service.dto.response.NotificationPreferenceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification/preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final QueryNotificationPreferenceService queryNotificationPreferenceService;
    private final UpdateNotificationPreferenceService updateNotificationPreferenceService;

    @GetMapping
    public NotificationPreferenceResponse queryMine() {
        return queryNotificationPreferenceService.queryMine();
    }

    @PatchMapping
    public NotificationPreferenceResponse update(@Valid @RequestBody NotificationPreferenceRequest request) {
        return updateNotificationPreferenceService.update(request);
    }
}
