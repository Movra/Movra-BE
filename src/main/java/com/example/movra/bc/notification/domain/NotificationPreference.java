package com.example.movra.bc.notification.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.notification.domain.exception.InvalidNotificationPreferenceException;
import com.example.movra.bc.notification.domain.vo.NotificationPreferenceId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import com.example.movra.sharedkernel.notification.NotificationType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_notification_preference", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationPreference extends AbstractAggregateRoot {

    public static final int DEFAULT_MAX_DAILY_PUSH_COUNT = 3;
    public static final LocalTime DEFAULT_SCHOOL_HOURS_START = LocalTime.of(8, 0);
    public static final LocalTime DEFAULT_SCHOOL_HOURS_END = LocalTime.of(15, 30);
    private static final int MIN_DAILY_PUSH_COUNT = 0;
    private static final int MAX_DAILY_PUSH_COUNT = 10;
    private static final LocalTime MIN_SCHOOL_HOURS_START = LocalTime.of(0, 0);
    private static final LocalTime MAX_SCHOOL_HOURS_END = LocalTime.of(22, 0);

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "notification_preference_id"))
    private NotificationPreferenceId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "daily_focus_enabled", nullable = false)
    private boolean dailyFocusEnabled;

    @Column(name = "daily_top_picks_enabled", nullable = false)
    private boolean dailyTopPicksEnabled;

    @Column(name = "daily_timetable_enabled", nullable = false)
    private boolean dailyTimetableEnabled;

    @Column(name = "accountability_enabled", nullable = false)
    private boolean accountabilityEnabled;

    @Column(name = "school_hours_quiet_enabled", nullable = false)
    private boolean schoolHoursQuietEnabled;

    @Column(name = "school_hours_start", nullable = false, columnDefinition = "TIME DEFAULT '08:00:00'")
    private LocalTime schoolHoursStart;

    @Column(name = "school_hours_end", nullable = false, columnDefinition = "TIME DEFAULT '15:30:00'")
    private LocalTime schoolHoursEnd;

    @Column(name = "weekend_school_quiet_enabled", nullable = false)
    private boolean weekendSchoolQuietEnabled;

    @Column(name = "sleep_hours_quiet_enabled", nullable = false)
    private boolean sleepHoursQuietEnabled;

    @Column(name = "max_daily_push_count", nullable = false)
    private int maxDailyPushCount;

    public static NotificationPreference createDefault(UserId userId) {
        if (userId == null) {
            throw new InvalidNotificationPreferenceException();
        }

        return NotificationPreference.builder()
                .id(NotificationPreferenceId.newId())
                .userId(userId)
                .dailyFocusEnabled(false)
                .dailyTopPicksEnabled(false)
                .dailyTimetableEnabled(false)
                .accountabilityEnabled(false)
                .schoolHoursQuietEnabled(true)
                .schoolHoursStart(DEFAULT_SCHOOL_HOURS_START)
                .schoolHoursEnd(DEFAULT_SCHOOL_HOURS_END)
                .weekendSchoolQuietEnabled(false)
                .sleepHoursQuietEnabled(true)
                .maxDailyPushCount(DEFAULT_MAX_DAILY_PUSH_COUNT)
                .build();
    }

    public void update(
            Boolean dailyFocusEnabled,
            Boolean dailyTopPicksEnabled,
            Boolean dailyTimetableEnabled,
            Boolean accountabilityEnabled,
            Boolean schoolHoursQuietEnabled,
            LocalTime schoolHoursStart,
            LocalTime schoolHoursEnd,
            Boolean weekendSchoolQuietEnabled,
            Boolean sleepHoursQuietEnabled,
            Integer maxDailyPushCount
    ) {
        validateFields(
                dailyFocusEnabled,
                dailyTopPicksEnabled,
                dailyTimetableEnabled,
                accountabilityEnabled,
                schoolHoursQuietEnabled,
                schoolHoursStart,
                schoolHoursEnd,
                weekendSchoolQuietEnabled,
                sleepHoursQuietEnabled,
                maxDailyPushCount
        );

        this.dailyFocusEnabled = dailyFocusEnabled;
        this.dailyTopPicksEnabled = dailyTopPicksEnabled;
        this.dailyTimetableEnabled = dailyTimetableEnabled;
        this.accountabilityEnabled = accountabilityEnabled;
        this.schoolHoursQuietEnabled = schoolHoursQuietEnabled;
        this.schoolHoursStart = schoolHoursStart;
        this.schoolHoursEnd = schoolHoursEnd;
        this.weekendSchoolQuietEnabled = weekendSchoolQuietEnabled;
        this.sleepHoursQuietEnabled = true;
        this.maxDailyPushCount = maxDailyPushCount;
    }

    public boolean allows(NotificationType notificationType) {
        return switch (notificationType) {
            case DAILY_FOCUS, RECOVERY, TIMING, D_DAY, STREAK -> dailyFocusEnabled;
            case DAILY_TOP_PICKS -> dailyTopPicksEnabled;
            case DAILY_TIMETABLE -> dailyTimetableEnabled;
            case ACCOUNTABILITY_DAILY_SUMMARY, ACCOUNTABILITY_MESSAGE -> accountabilityEnabled;
        };
    }

    private static void validateFields(
            Boolean dailyFocusEnabled,
            Boolean dailyTopPicksEnabled,
            Boolean dailyTimetableEnabled,
            Boolean accountabilityEnabled,
            Boolean schoolHoursQuietEnabled,
            LocalTime schoolHoursStart,
            LocalTime schoolHoursEnd,
            Boolean weekendSchoolQuietEnabled,
            Boolean sleepHoursQuietEnabled,
            Integer maxDailyPushCount
    ) {
        if (dailyFocusEnabled == null
                || dailyTopPicksEnabled == null
                || dailyTimetableEnabled == null
                || accountabilityEnabled == null
                || schoolHoursQuietEnabled == null
                || schoolHoursStart == null
                || schoolHoursEnd == null
                || weekendSchoolQuietEnabled == null
                || sleepHoursQuietEnabled == null
                || maxDailyPushCount == null) {
            throw new InvalidNotificationPreferenceException();
        }

        if (!sleepHoursQuietEnabled) {
            throw new InvalidNotificationPreferenceException();
        }

        if (maxDailyPushCount < MIN_DAILY_PUSH_COUNT || maxDailyPushCount > MAX_DAILY_PUSH_COUNT) {
            throw new InvalidNotificationPreferenceException();
        }

        if (schoolHoursStart.isBefore(MIN_SCHOOL_HOURS_START)
                || schoolHoursEnd.isAfter(MAX_SCHOOL_HOURS_END)
                || !schoolHoursStart.isBefore(schoolHoursEnd)) {
            throw new InvalidNotificationPreferenceException();
        }
    }
}
