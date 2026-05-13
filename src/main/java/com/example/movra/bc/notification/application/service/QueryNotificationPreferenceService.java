package com.example.movra.bc.notification.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.application.service.dto.response.NotificationPreferenceResponse;
import com.example.movra.bc.notification.domain.NotificationPreference;
import com.example.movra.bc.notification.domain.repository.NotificationPreferenceRepository;
import com.example.movra.config.cache.HomeCacheNames;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryNotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final CurrentUserQuery currentUserQuery;

    @Cacheable(
            cacheNames = HomeCacheNames.NOTIFICATION_PREFERENCE,
            key = "@homeCacheKey.currentUserId()",
            sync = true
    )
    @Transactional
    public NotificationPreferenceResponse queryMine() {
        UserId userId = currentUserQuery.currentUser().userId();
        NotificationPreference preference = notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> notificationPreferenceRepository.save(NotificationPreference.createDefault(userId)));

        return NotificationPreferenceResponse.from(preference);
    }
}
