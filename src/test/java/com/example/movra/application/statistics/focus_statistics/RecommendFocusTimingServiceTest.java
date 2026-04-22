package com.example.movra.application.statistics.focus_statistics;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreference;
import com.example.movra.bc.statistics.focus_statistics.application.service.RecommendFocusTimingService;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimingRecommendationResponse;
import com.example.movra.bc.statistics.focus_statistics.application.service.dto.response.FocusTimingRecommendationResponse.RecommendedHour;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.FocusStatisticsReadPort;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryItemView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryView;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RecommendFocusTimingServiceTest {

    @Mock
    private FocusStatisticsReadPort focusStatisticsReadPort;

    @Mock
    private BehaviorProfileRepository behaviorProfileRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    private RecommendFocusTimingService recommendFocusTimingService;

    private final UserId userId = UserId.newId();
    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");

    @BeforeEach
    void setUp() {
        recommendFocusTimingService = new RecommendFocusTimingService(
                focusStatisticsReadPort,
                behaviorProfileRepository,
                currentUserQuery,
                clock
        );
    }

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
        lenient().when(clock.getZone()).thenReturn(zoneId);
    }

    @Test
    @DisplayName("recommend returns top hours based on sufficient weekday data")
    void recommend_withSufficientData_returnsTopHours() {
        givenCurrentUser();
        // 2026-04-21 is Tuesday (weekday)
        LocalDate today = LocalDate.of(2026, 4, 21);
        Instant now = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, zoneId).toInstant();
        given(clock.instant()).willReturn(now);

        LocalDate from = today.minusDays(14);
        LocalDate to = today.minusDays(1);

        // Create summaries for several weekdays with focus at 9am and 2pm
        // April 8 (Wed), April 9 (Thu), April 10 (Fri), April 13 (Mon), April 14 (Tue)
        List<FocusStatisticsSummaryView> summaries = List.of(
                weekdaySummary(LocalDate.of(2026, 4, 8)),
                weekdaySummary(LocalDate.of(2026, 4, 9)),
                weekdaySummary(LocalDate.of(2026, 4, 10)),
                weekdaySummary(LocalDate.of(2026, 4, 13)),
                weekdaySummary(LocalDate.of(2026, 4, 14))
        );

        given(focusStatisticsReadPort.findSummaryRange(userId, from, to)).willReturn(summaries);

        FocusTimingRecommendationResponse response = recommendFocusTimingService.recommend();

        assertThat(response.targetDate()).isEqualTo(today);
        assertThat(response.basedOnData()).isTrue();
        assertThat(response.recommendedHours()).isNotEmpty();
        assertThat(response.recommendedHours()).hasSizeLessThanOrEqualTo(2);

        // 9am and 14pm should be recommended (each has 1800s per day, 10 weekdays in range -> avg 900s)
        // Actually: 5 summaries with data out of 10 weekdays. 9am: 5*1800/10=900, 14pm: 5*1800/10=900
        // 900 >= 600 threshold, so both should be present
        List<Integer> recommendedHourValues = response.recommendedHours().stream()
                .map(RecommendedHour::hourOfDay)
                .toList();
        assertThat(recommendedHourValues).contains(9, 14);

        for (RecommendedHour hour : response.recommendedHours()) {
            assertThat(hour.averageFocusSeconds()).isGreaterThanOrEqualTo(600L);
        }

        assertThat(response.reason()).contains("평일");
        then(behaviorProfileRepository).should(never()).findByUserId(any());
    }

    @Test
    @DisplayName("recommend filters weekend data correctly when today is weekend")
    void recommend_weekendData_filtersCorrectly() {
        givenCurrentUser();
        // 2026-04-18 is Saturday (weekend)
        LocalDate today = LocalDate.of(2026, 4, 18);
        Instant now = ZonedDateTime.of(2026, 4, 18, 10, 0, 0, 0, zoneId).toInstant();
        given(clock.instant()).willReturn(now);

        LocalDate from = today.minusDays(14);
        LocalDate to = today.minusDays(1);

        // Mix weekday and weekend summaries - only weekend ones should be used
        FocusStatisticsSummaryView weekdaySummary = createSummary(
                LocalDate.of(2026, 4, 13), // Monday
                9, 1800L
        );
        FocusStatisticsSummaryView weekendSummary1 = createSummary(
                LocalDate.of(2026, 4, 11), // Saturday
                20, 3600L
        );
        FocusStatisticsSummaryView weekendSummary2 = createSummary(
                LocalDate.of(2026, 4, 12), // Sunday
                20, 3600L
        );

        given(focusStatisticsReadPort.findSummaryRange(userId, from, to))
                .willReturn(List.of(weekdaySummary, weekendSummary1, weekendSummary2));

        FocusTimingRecommendationResponse response = recommendFocusTimingService.recommend();

        assertThat(response.basedOnData()).isTrue();
        assertThat(response.recommendedHours()).isNotEmpty();

        // Should only contain hour 20 from weekend data, not hour 9 from weekday
        List<Integer> recommendedHourValues = response.recommendedHours().stream()
                .map(RecommendedHour::hourOfDay)
                .toList();
        assertThat(recommendedHourValues).contains(20);
        assertThat(recommendedHourValues).doesNotContain(9);
        assertThat(response.reason()).contains("주말");
    }

    @Test
    @DisplayName("recommend falls back to behavior profile when data is insufficient")
    void recommend_insufficientData_fallsToBehaviorProfile() {
        givenCurrentUser();
        LocalDate today = LocalDate.of(2026, 4, 21);
        Instant now = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, zoneId).toInstant();
        given(clock.instant()).willReturn(now);

        LocalDate from = today.minusDays(14);
        LocalDate to = today.minusDays(1);

        given(focusStatisticsReadPort.findSummaryRange(userId, from, to)).willReturn(List.of());

        BehaviorProfile profile = BehaviorProfile.create(
                userId,
                ExecutionDifficulty.MEDIUM,
                SocialPreference.LOW,
                RecoveryStyle.QUICK_RESTART,
                9,
                21,
                CoachingMode.GENTLE
        );
        given(behaviorProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

        FocusTimingRecommendationResponse response = recommendFocusTimingService.recommend();

        assertThat(response.basedOnData()).isFalse();
        assertThat(response.recommendedHours()).hasSize(2);

        List<Integer> hourValues = response.recommendedHours().stream()
                .map(RecommendedHour::hourOfDay)
                .toList();
        assertThat(hourValues).containsExactly(9, 21);
        assertThat(response.reason()).contains("선호 시간대");
    }

    @Test
    @DisplayName("recommend returns empty when no data and no behavior profile")
    void recommend_noDataNoBehaviorProfile_returnsEmpty() {
        givenCurrentUser();
        LocalDate today = LocalDate.of(2026, 4, 21);
        Instant now = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, zoneId).toInstant();
        given(clock.instant()).willReturn(now);

        LocalDate from = today.minusDays(14);
        LocalDate to = today.minusDays(1);

        given(focusStatisticsReadPort.findSummaryRange(userId, from, to)).willReturn(List.of());
        given(behaviorProfileRepository.findByUserId(userId)).willReturn(Optional.empty());

        FocusTimingRecommendationResponse response = recommendFocusTimingService.recommend();

        assertThat(response.basedOnData()).isFalse();
        assertThat(response.recommendedHours()).isEmpty();
        assertThat(response.targetDate()).isEqualTo(today);
        assertThat(response.reason()).contains("데이터가 아직 없어요");
    }

    /**
     * Creates a weekday summary with 30min focus at 9am and 30min focus at 2pm.
     */
    private FocusStatisticsSummaryView weekdaySummary(LocalDate date) {
        return new FocusStatisticsSummaryView(
                date,
                3600L,
                List.of(
                        new FocusStatisticsSummaryItemView(
                                ZonedDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                                        9, 0, 0, 0, zoneId).toInstant(),
                                ZonedDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                                        9, 30, 0, 0, zoneId).toInstant()
                        ),
                        new FocusStatisticsSummaryItemView(
                                ZonedDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                                        14, 0, 0, 0, zoneId).toInstant(),
                                ZonedDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                                        14, 30, 0, 0, zoneId).toInstant()
                        )
                )
        );
    }

    private FocusStatisticsSummaryView createSummary(LocalDate date, int hour, long seconds) {
        return new FocusStatisticsSummaryView(
                date,
                seconds,
                List.of(
                        new FocusStatisticsSummaryItemView(
                                ZonedDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                                        hour, 0, 0, 0, zoneId).toInstant(),
                                ZonedDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                                        hour, 0, 0, 0, zoneId).plusSeconds(seconds).toInstant()
                        )
                )
        );
    }
}
