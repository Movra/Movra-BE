package com.example.movra.bc.statistics.focus_statistics.application.service.support;

import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

@Component
public class FocusStatisticsPeriodResolver {

    public FocusStatisticsPeriod resolveDay(LocalDate targetDate, ZoneId zoneId) {
        return create(targetDate, targetDate, zoneId);
    }

    public FocusStatisticsPeriod resolveWeek(LocalDate targetDate, ZoneId zoneId) {
        LocalDate startDate = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(6);
        return create(startDate, endDate, zoneId);
    }

    public FocusStatisticsPeriod resolveMonth(LocalDate targetDate, ZoneId zoneId) {
        LocalDate startDate = targetDate.withDayOfMonth(1);
        LocalDate endDate = targetDate.with(TemporalAdjusters.lastDayOfMonth());
        return create(startDate, endDate, zoneId);
    }

    public FocusStatisticsPeriod resolveRange(LocalDate startDate, LocalDate endDate, ZoneId zoneId) {
        return create(startDate, endDate, zoneId);
    }

    private FocusStatisticsPeriod create(LocalDate startDate, LocalDate endDate, ZoneId zoneId) {
        int dayCount = (int) ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));

        return new FocusStatisticsPeriod(
                startDate,
                endDate,
                startDate.atStartOfDay(zoneId).toInstant(),
                endDate.plusDays(1).atStartOfDay(zoneId).toInstant(),
                dayCount
        );
    }
}
