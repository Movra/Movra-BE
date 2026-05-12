package com.example.movra.bc.notification.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.notification.application.service.dto.request.NotificationPreferenceRequest;
import com.example.movra.bc.notification.application.service.dto.response.NotificationPreferenceResponse;
import com.example.movra.bc.notification.domain.NotificationPreference;
import com.example.movra.bc.notification.domain.repository.NotificationPreferenceRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UpdateNotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional
    public NotificationPreferenceResponse update(NotificationPreferenceRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        NotificationPreference preference = notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreference.createDefault(userId));

        boolean previousSchoolHoursQuietEnabled = preference.isSchoolHoursQuietEnabled();

        preference.update(
                request.dailyFocusEnabled(),
                request.dailyTopPicksEnabled(),
                request.dailyTimetableEnabled(),
                request.accountabilityEnabled(),
                request.schoolHoursQuietEnabled(),
                request.schoolHoursStart(),
                request.schoolHoursEnd(),
                request.weekendSchoolQuietEnabled(),
                request.sleepHoursQuietEnabled(),
                request.maxDailyPushCount()
        );

        NotificationPreference saved = notificationPreferenceRepository.save(preference);

        if (previousSchoolHoursQuietEnabled != saved.isSchoolHoursQuietEnabled()) {
            analyticsEventRecorder.recordSafely(
                    userId,
                    AnalyticsEventType.SCHOOL_HOURS_MUTE_TOGGLED,
                    Map.of("enabled", String.valueOf(saved.isSchoolHoursQuietEnabled()))
            );
        }

        return NotificationPreferenceResponse.from(saved);
    }
}
