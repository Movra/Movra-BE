package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.QueryRecoveryCardService;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.RecoveryCardResponse;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreference;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QueryRecoveryCardServiceTest {

    @InjectMocks
    private QueryRecoveryCardService queryRecoveryCardService;

    @Mock
    private DailyFocusSummaryRepository dailyFocusSummaryRepository;

    @Mock
    private DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;

    @Mock
    private BehaviorProfileRepository behaviorProfileRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    private final UserId userId = UserId.newId();
    private final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    private final LocalDate today = LocalDate.of(2026, 4, 12);
    private final LocalDate yesterday = today.minusDays(1);

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    private void givenClock() {
        lenient().when(clock.instant()).thenReturn(
                ZonedDateTime.of(2026, 4, 12, 10, 0, 0, 0, zoneId).toInstant()
        );
        lenient().when(clock.getZone()).thenReturn(zoneId);
    }

    private BehaviorProfile behaviorProfileWithStyle(RecoveryStyle style) {
        return BehaviorProfile.create(
                userId,
                ExecutionDifficulty.values()[0],
                SocialPreference.values()[0],
                style,
                9,
                18,
                CoachingMode.values()[0]
        );
    }

    private DailyFocusSummary stubFocusSummary(long totalSeconds) {
        try {
            var constructor = DailyFocusSummary.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            DailyFocusSummary summary = constructor.newInstance();
            ReflectionTestUtils.setField(summary, "totalSeconds", totalSeconds);
            return summary;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DailyTopPicksSummary stubTopPicksSummary(int totalCount, int completedCount) {
        try {
            var constructor = DailyTopPicksSummary.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            DailyTopPicksSummary summary = constructor.newInstance();
            ReflectionTestUtils.setField(summary, "totalCount", totalCount);
            ReflectionTestUtils.setField(summary, "completedCount", completedCount);
            return summary;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("query returns MISSED_FOCUS when no focus summary exists for yesterday")
    void query_missedFocusYesterday_returnsNeedsRecovery() {
        givenCurrentUser();
        givenClock();
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfileWithStyle(RecoveryStyle.QUICK_RESTART)));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.needsRecovery()).isTrue();
        assertThat(response.recoveryType()).isEqualTo(RecoveryType.MISSED_FOCUS);
        assertThat(response.suggestedAction()).isEqualTo("어제는 쉬어갔어요. 지금 바로 시작해볼까요?");
        assertThat(response.yesterdayFocusSeconds()).isEqualTo(0L);
    }

    @Test
    @DisplayName("query returns INCOMPLETE_TOP_PICK when focus exists but top picks are incomplete")
    void query_incompleteTopPickYesterday_returnsNeedsRecovery() {
        givenCurrentUser();
        givenClock();
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubFocusSummary(3600)));
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubTopPicksSummary(3, 1)));
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfileWithStyle(RecoveryStyle.NEEDS_REFLECTION)));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.needsRecovery()).isTrue();
        assertThat(response.recoveryType()).isEqualTo(RecoveryType.INCOMPLETE_TOP_PICK);
        assertThat(response.suggestedAction()).isEqualTo("어제 무엇이 어려웠는지 한 줄만 남겨볼까요?");
        assertThat(response.yesterdayFocusSeconds()).isEqualTo(3600L);
        assertThat(response.yesterdayTopPickCompletionRate()).isCloseTo(1.0 / 3.0, Offset.offset(0.001));
    }

    @Test
    @DisplayName("query returns BOTH when focus missed and top picks incomplete")
    void query_bothMissed_returnsBoth() {
        givenCurrentUser();
        givenClock();
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubTopPicksSummary(2, 1)));
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfileWithStyle(RecoveryStyle.SLOW_REBUILDER)));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.needsRecovery()).isTrue();
        assertThat(response.recoveryType()).isEqualTo(RecoveryType.BOTH);
        assertThat(response.suggestedAction()).isEqualTo("5분만 해볼까요? 작게 시작하면 돼요.");
        assertThat(response.yesterdayFocusSeconds()).isEqualTo(0L);
        assertThat(response.yesterdayTopPickCompletionRate()).isCloseTo(0.5, Offset.offset(0.001));
    }

    @Test
    @DisplayName("query returns NONE when yesterday was normal")
    void query_normalYesterday_returnsNoRecovery() {
        givenCurrentUser();
        givenClock();
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubFocusSummary(7200)));
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubTopPicksSummary(3, 3)));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.needsRecovery()).isFalse();
        assertThat(response.recoveryType()).isEqualTo(RecoveryType.NONE);
        assertThat(response.suggestedAction()).isNull();
        assertThat(response.yesterdayFocusSeconds()).isEqualTo(7200L);
        assertThat(response.yesterdayTopPickCompletionRate()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("query returns default message when no behavior profile exists")
    void query_noBehaviorProfile_returnsDefaultMessage() {
        givenCurrentUser();
        givenClock();
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.empty());

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.needsRecovery()).isTrue();
        assertThat(response.recoveryType()).isEqualTo(RecoveryType.MISSED_FOCUS);
        assertThat(response.suggestedAction()).isEqualTo("다시 시작해볼까요?");
    }
}
