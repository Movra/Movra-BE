package com.example.movra.bc.statistics.focus_statistics.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusPeriodStatisticsResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusPeriodStatisticsCalculator;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsPeriodResolver;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusPeriodStatisticsResult;
import com.example.movra.config.cache.StatsCacheNames;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class QueryFocusPeriodStatisticsService {

    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;
    private final FocusStatisticsPeriodResolver focusStatisticsPeriodResolver;
    private final FocusPeriodStatisticsCalculator focusPeriodStatisticsCalculator;

    // FINAL 상태(오늘 이전 날짜)만 캐시: 확정된 데이터는 이후 절대 바뀌지 않는다.
    // PARTIAL(오늘 포함) 은 condition=false 로 캐시를 건너뛴다.
    @Cacheable(
            cacheNames = StatsCacheNames.FOCUS_STATS_DAILY,
            key = "@statsCacheKey.forStats(#targetDate)",
            condition = "@statsCacheKey.isFinalDay(#targetDate)"
    )
    @Transactional(readOnly = true)
    public FocusPeriodStatisticsResponse queryDaily(LocalDate targetDate) {
        return query(targetDate, focusStatisticsPeriodResolver::resolveDay);
    }

    @Cacheable(
            cacheNames = StatsCacheNames.FOCUS_STATS_WEEKLY,
            key = "@statsCacheKey.forStats(#targetDate)",
            condition = "@statsCacheKey.isFinalWeek(#targetDate)"
    )
    @Transactional(readOnly = true)
    public FocusPeriodStatisticsResponse queryWeekly(LocalDate targetDate) {
        return query(targetDate, focusStatisticsPeriodResolver::resolveWeek);
    }

    @Cacheable(
            cacheNames = StatsCacheNames.FOCUS_STATS_MONTHLY,
            key = "@statsCacheKey.forStats(#targetDate)",
            condition = "@statsCacheKey.isFinalMonth(#targetDate)"
    )
    @Transactional(readOnly = true)
    public FocusPeriodStatisticsResponse queryMonthly(LocalDate targetDate) {
        return query(targetDate, focusStatisticsPeriodResolver::resolveMonth);
    }

    private FocusPeriodStatisticsResponse query(
            LocalDate targetDate,
            BiFunction<LocalDate, ZoneId, FocusStatisticsPeriod> periodResolver
    ) {
        Instant now = clock.instant();
        UserId userId = currentUserQuery.currentUser().userId();
        ZoneId zoneId = clock.getZone();
        FocusStatisticsPeriod period = periodResolver.apply(targetDate, zoneId);
        FocusPeriodStatisticsResult result = focusPeriodStatisticsCalculator.calculate(userId, period, now, zoneId);

        return FocusPeriodStatisticsResponse.from(targetDate, now, result);
    }
}
