package com.example.movra.bc.statistics.focus_statistics.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimingRecommendationResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimingRecommendationResponse.RecommendedHour;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsReadPort;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryItemView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryView;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendFocusTimingService {

    private static final int LOOKBACK_DAYS = 14;
    private static final int MAX_RECOMMENDATIONS = 2;
    private static final long MIN_THRESHOLD_SECONDS = 600L;
    private static final int HOURS_PER_DAY = 24;

    private final FocusStatisticsReadPort focusStatisticsReadPort;
    private final BehaviorProfileRepository behaviorProfileRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional(readOnly = true)
    public FocusTimingRecommendationResponse recommend() {
        Instant now = clock.instant();
        ZoneId zoneId = clock.getZone();
        LocalDate today = now.atZone(zoneId).toLocalDate();
        UserId userId = currentUserQuery.currentUser().userId();

        LocalDate from = today.minusDays(LOOKBACK_DAYS);
        LocalDate to = today.minusDays(1);

        List<FocusStatisticsSummaryView> summaries = focusStatisticsReadPort.findSummaryRange(userId, from, to);

        boolean todayIsWeekend = isWeekend(today);

        Map<LocalDate, FocusStatisticsSummaryView> filteredSummaries = summaries.stream()
                .filter(s -> isWeekend(s.date()) == todayIsWeekend)
                .collect(Collectors.toMap(FocusStatisticsSummaryView::date, s -> s));

        long matchingDayCount = countMatchingDays(from, to, todayIsWeekend);

        if (matchingDayCount == 0 || filteredSummaries.isEmpty()) {
            return buildFallbackResponse(today, now, userId);
        }

        long[] hourlyTotalSeconds = new long[HOURS_PER_DAY];

        for (FocusStatisticsSummaryView summary : filteredSummaries.values()) {
            accumulateHourlySeconds(hourlyTotalSeconds, summary, zoneId);
        }

        List<RecommendedHour> candidates = new ArrayList<>();
        for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
            long averageSeconds = hourlyTotalSeconds[hour] / matchingDayCount;
            if (averageSeconds >= MIN_THRESHOLD_SECONDS) {
                candidates.add(new RecommendedHour(hour, averageSeconds));
            }
        }

        if (candidates.isEmpty()) {
            return buildFallbackResponse(today, now, userId);
        }

        candidates.sort(Comparator.comparingLong(RecommendedHour::averageFocusSeconds).reversed());

        List<RecommendedHour> topHours = candidates.stream()
                .limit(MAX_RECOMMENDATIONS)
                .sorted(Comparator.comparingInt(RecommendedHour::hourOfDay))
                .toList();

        String reason = buildReason(topHours, todayIsWeekend);

        return FocusTimingRecommendationResponse.builder()
                .targetDate(today)
                .queriedAt(now)
                .recommendedHours(topHours)
                .reason(reason)
                .basedOnData(true)
                .build();
    }

    private void accumulateHourlySeconds(
            long[] hourlyTotalSeconds,
            FocusStatisticsSummaryView summary,
            ZoneId zoneId
    ) {
        for (FocusStatisticsSummaryItemView item : summary.items()) {
            Instant cursor = item.overlapStartedAt();
            Instant end = item.overlapEndedAt();

            while (cursor.isBefore(end)) {
                ZonedDateTime zonedCursor = cursor.atZone(zoneId);
                ZonedDateTime nextHour = zonedCursor.withMinute(0).withSecond(0).withNano(0).plusHours(1);
                Instant effectiveEnd = nextHour.toInstant().isBefore(end)
                        ? nextHour.toInstant()
                        : end;

                hourlyTotalSeconds[zonedCursor.getHour()] += Duration.between(cursor, effectiveEnd).getSeconds();

                cursor = effectiveEnd;
            }
        }
    }

    private long countMatchingDays(LocalDate from, LocalDate to, boolean weekend) {
        long count = 0;
        LocalDate date = from;
        while (!date.isAfter(to)) {
            if (isWeekend(date) == weekend) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private FocusTimingRecommendationResponse buildFallbackResponse(
            LocalDate today,
            Instant now,
            UserId userId
    ) {
        return behaviorProfileRepository.findByUserId(userId)
                .map(profile -> buildFromBehaviorProfile(today, now, profile))
                .orElseGet(() -> emptyResponse(today, now));
    }

    private FocusTimingRecommendationResponse buildFromBehaviorProfile(
            LocalDate today,
            Instant now,
            BehaviorProfile profile
    ) {
        int startHour = profile.getPreferredFocusStartHour();
        int endHour = profile.getPreferredFocusEndHour();

        List<RecommendedHour> hours = new ArrayList<>();
        hours.add(new RecommendedHour(startHour, 0L));
        if (endHour != startHour) {
            hours.add(new RecommendedHour(endHour, 0L));
        }

        hours.sort(Comparator.comparingInt(RecommendedHour::hourOfDay));

        return FocusTimingRecommendationResponse.builder()
                .targetDate(today)
                .queriedAt(now)
                .recommendedHours(hours)
                .reason("최근 집중 데이터가 부족하여 설정된 선호 시간대를 기반으로 추천해요")
                .basedOnData(false)
                .build();
    }

    private FocusTimingRecommendationResponse emptyResponse(LocalDate today, Instant now) {
        return FocusTimingRecommendationResponse.builder()
                .targetDate(today)
                .queriedAt(now)
                .recommendedHours(List.of())
                .reason("추천할 수 있는 데이터가 아직 없어요")
                .basedOnData(false)
                .build();
    }

    private String buildReason(List<RecommendedHour> topHours, boolean weekend) {
        String dayType = weekend ? "주말" : "평일";
        String timeDescription = topHours.stream()
                .map(h -> h.hourOfDay() + "시")
                .collect(Collectors.joining(", "));

        return String.format("최근 2주 %s 기준 %s 시간대의 집중 시간이 가장 길어요", dayType, timeDescription);
    }
}
