package com.example.movra.bc.statistics.focus_statistics.application.service.dto.response;

public record FocusTimeBucketResponse(
        int hourOfDay,
        long focusSeconds
) {
}
