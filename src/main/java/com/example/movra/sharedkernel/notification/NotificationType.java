package com.example.movra.sharedkernel.notification;

public enum NotificationType {
    DAILY_FOCUS,
    DAILY_TOP_PICKS,
    DAILY_TIMETABLE,
    ACCOUNTABILITY_DAILY_SUMMARY,
    ACCOUNTABILITY_MESSAGE,
    RECOVERY,
    TIMING,
    D_DAY,
    STREAK,
    INSIGHT_REPORT_READY;

    public int dailyTypeLimit() {
        return switch (this) {
            case RECOVERY, TIMING, STREAK -> 1;
            case D_DAY -> 3;
            default -> Integer.MAX_VALUE;
        };
    }
}
