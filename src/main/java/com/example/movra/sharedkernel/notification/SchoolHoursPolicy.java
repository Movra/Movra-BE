package com.example.movra.sharedkernel.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class SchoolHoursPolicy {

    private static final LocalTime SCHOOL_HOURS_START = LocalTime.of(8, 0);
    private static final LocalTime SCHOOL_HOURS_END = LocalTime.of(15, 30);

    private final Clock clock;

    public boolean isSchoolHoursNow() {
        LocalDateTime now = LocalDateTime.now(clock);
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        LocalTime time = now.toLocalTime();
        return !time.isBefore(SCHOOL_HOURS_START) && time.isBefore(SCHOOL_HOURS_END);
    }
}
