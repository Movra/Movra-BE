package com.example.movra.bc.notification.application.service.dto.response;

import com.example.movra.bc.notification.domain.NotificationPreference;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalTime;
import java.util.UUID;

@Builder
public record NotificationPreferenceResponse(
        UUID notificationPreferenceId,
        boolean dailyFocusEnabled,
        boolean dailyTopPicksEnabled,
        boolean dailyTimetableEnabled,
        boolean accountabilityEnabled,
        boolean schoolHoursQuietEnabled,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime schoolHoursStart,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime schoolHoursEnd,
        boolean weekendSchoolQuietEnabled,
        boolean sleepHoursQuietEnabled,
        int maxDailyPushCount
) {

    public static NotificationPreferenceResponse from(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .notificationPreferenceId(preference.getId().id())
                .dailyFocusEnabled(preference.isDailyFocusEnabled())
                .dailyTopPicksEnabled(preference.isDailyTopPicksEnabled())
                .dailyTimetableEnabled(preference.isDailyTimetableEnabled())
                .accountabilityEnabled(preference.isAccountabilityEnabled())
                .schoolHoursQuietEnabled(preference.isSchoolHoursQuietEnabled())
                .schoolHoursStart(preference.getSchoolHoursStart())
                .schoolHoursEnd(preference.getSchoolHoursEnd())
                .weekendSchoolQuietEnabled(preference.isWeekendSchoolQuietEnabled())
                .sleepHoursQuietEnabled(preference.isSleepHoursQuietEnabled())
                .maxDailyPushCount(preference.getMaxDailyPushCount())
                .build();
    }
}
