package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DailyTimetableSummaryTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    @DisplayName("close rejects a timetable from another daily plan")
    void close_mismatchedTimetable_throwsIllegalArgumentException() {
        UserId userId = UserId.newId();
        LocalDate date = LocalDate.of(2026, 4, 14);
        DailyPlan dailyPlan = DailyPlan.create(userId, date);
        DailyPlan anotherDailyPlan = DailyPlan.create(userId, date.plusDays(1));
        Timetable timetable = Timetable.create(anotherDailyPlan.getDailyPlanId(), 0);

        assertThatThrownBy(() -> DailyTimetableSummary.close(dailyPlan, timetable, clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Timetable must belong to the given DailyPlan.");
    }
}
