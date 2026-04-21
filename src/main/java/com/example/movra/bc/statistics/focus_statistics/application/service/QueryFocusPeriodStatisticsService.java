package com.example.movra.bc.statistics.focus_statistics.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusPeriodStatisticsResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusPeriodStatisticsCalculator;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsPeriodResolver;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusPeriodStatisticsResult;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public FocusPeriodStatisticsResponse queryDaily(LocalDate targetDate) {
        return query(targetDate, focusStatisticsPeriodResolver::resolveDay);
    }

    @Transactional(readOnly = true)
    public FocusPeriodStatisticsResponse queryWeekly(LocalDate targetDate) {
        return query(targetDate, focusStatisticsPeriodResolver::resolveWeek);
    }

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
