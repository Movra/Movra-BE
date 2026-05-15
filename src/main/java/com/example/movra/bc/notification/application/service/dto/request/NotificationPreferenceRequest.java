package com.example.movra.bc.notification.application.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record NotificationPreferenceRequest(
        @NotNull
        Boolean dailyFocusEnabled,

        @NotNull
        Boolean dailyTopPicksEnabled,

        @NotNull
        Boolean dailyTimetableEnabled,

        @NotNull
        Boolean accountabilityEnabled,

        @NotNull
        Boolean schoolHoursQuietEnabled,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime schoolHoursStart,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime schoolHoursEnd,

        @NotNull
        Boolean weekendSchoolQuietEnabled,

        @NotNull
        @Min(0) @Max(10)
        Integer maxDailyPushCount
) {
}
