package com.example.movra.bc.statistics.focus_statistics.application.service.support.dto;

public record FocusTimeBucketView(
        int hourOfDay,
        long focusSeconds
) {
}
