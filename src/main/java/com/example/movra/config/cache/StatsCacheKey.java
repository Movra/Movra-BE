package com.example.movra.config.cache;

import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
@RequiredArgsConstructor
public class StatsCacheKey {

    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    public String forStats(LocalDate targetDate) {
        return currentUserQuery.currentUser().userId().id() + ":" + targetDate;
    }

    // 해당 날짜 하루가 오늘 이전이면 데이터가 확정(FINAL) 상태
    public boolean isFinalDay(LocalDate targetDate) {
        return targetDate.isBefore(LocalDate.now(clock));
    }

    // 해당 날짜가 속한 주(월~일)의 마지막 날이 오늘 이전이면 FINAL
    public boolean isFinalWeek(LocalDate targetDate) {
        LocalDate weekEnd = targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return weekEnd.isBefore(LocalDate.now(clock));
    }

    // 해당 날짜가 속한 월의 마지막 날이 오늘 이전이면 FINAL
    public boolean isFinalMonth(LocalDate targetDate) {
        LocalDate monthEnd = targetDate.with(TemporalAdjusters.lastDayOfMonth());
        return monthEnd.isBefore(LocalDate.now(clock));
    }
}
