package com.example.movra.bc.notification.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.application.service.dto.response.NotificationPreferenceResponse;
import com.example.movra.bc.notification.domain.NotificationPreference;
import com.example.movra.bc.notification.domain.repository.NotificationPreferenceRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryNotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationPreferenceProvisioner notificationPreferenceProvisioner;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse queryMine() {
        UserId userId = currentUserQuery.currentUser().userId();
        NotificationPreference preference = notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> notificationPreferenceProvisioner.createOrLoad(userId));

        return NotificationPreferenceResponse.from(preference);
    }
}
