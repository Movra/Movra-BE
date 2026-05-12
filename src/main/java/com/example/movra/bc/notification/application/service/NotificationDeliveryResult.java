package com.example.movra.bc.notification.application.service;

public enum NotificationDeliveryResult {
    SENT,
    SKIPPED_PREFERENCE_DISABLED,
    SKIPPED_SCHOOL_HOURS,
    SKIPPED_SLEEP_HOURS,
    SKIPPED_DAILY_LIMIT_EXCEEDED,
    SKIPPED_TYPE_DAILY_LIMIT_EXCEEDED,
    FAILED
}
