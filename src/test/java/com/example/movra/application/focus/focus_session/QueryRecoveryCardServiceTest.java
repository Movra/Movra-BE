package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.service.QueryRecoveryCardService;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.RecoveryCardResponse;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import com.example.movra.bc.personalization.behavior_profile.domain.BehaviorProfile;
import com.example.movra.bc.personalization.behavior_profile.domain.repository.BehaviorProfileRepository;
import com.example.movra.bc.personalization.behavior_profile.domain.type.CoachingMode;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExamTrack;
import com.example.movra.bc.personalization.behavior_profile.domain.type.ExecutionDifficulty;
import com.example.movra.bc.personalization.behavior_profile.domain.type.RecoveryStyle;
import com.example.movra.bc.personalization.behavior_profile.domain.type.SocialPreference;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.repository.ExamScheduleRepository;
import com.example.movra.bc.planning.exam_schedule.domain.type.ExamType;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QueryRecoveryCardServiceTest {

    @InjectMocks
    private QueryRecoveryCardService queryRecoveryCardService;

    @Mock
    private DailyFocusSummaryRepository dailyFocusSummaryRepository;

    @Mock
    private FocusSessionRepository focusSessionRepository;

    @Mock
    private DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;

    @Mock
    private BehaviorProfileRepository behaviorProfileRepository;

    @Mock
    private ExamScheduleRepository examScheduleRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private Clock clock;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

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
        lenient().when(examScheduleRepository.findFirstByUserIdAndExamDateBetweenOrderByExamDateDesc(
                userId,
                today.minusDays(7),
                today.minusDays(1)
        )).thenReturn(Optional.empty());
        lenient().when(focusSessionRepository.findFirstByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(userId))
                .thenReturn(Optional.empty());
    }

    private BehaviorProfile behaviorProfileWithStyle(RecoveryStyle style) {
        return behaviorProfileWith(style, CoachingMode.NEUTRAL);
    }

    private BehaviorProfile behaviorProfileWith(RecoveryStyle style, CoachingMode coachingMode) {
        return BehaviorProfile.create(
                userId,
                ExecutionDifficulty.values()[0],
                SocialPreference.values()[0],
                style,
                ExamTrack.NAESIN,
                9,
                18,
                coachingMode
        );
    }

    private ExamSchedule examSchedule(LocalDate examDate) {
        return ExamSchedule.create(userId, ExamType.NAESIN, "중간고사", examDate, "수학",
                Clock.fixed(today.atStartOfDay(zoneId).toInstant(), zoneId));
    }

    private FocusSession completedFocusSession(LocalDate endedDate) {
        FocusSession focusSession = FocusSession.start(
                userId,
                endedDate.atStartOfDay(zoneId).toInstant(),
                5
        );
        focusSession.complete(endedDate.atTime(0, 30).atZone(zoneId).toInstant());
        return focusSession;
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
        assertThat(response.postExamMode()).isFalse();
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.RECOVERY_CARD_VIEWED),
                argThat(properties ->
                        properties.get("recoveryType").equals(RecoveryType.MISSED_FOCUS.name())
                                && properties.get("needsRecovery").equals("true")
                                && properties.get("targetDate").equals(yesterday.toString())
                                && properties.get("postExamMode").equals("false")
                )
        );
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
        assertThat(response.suggestedAction()).isEqualTo("어제 무엇이 어려웠는지 한 줄만 적어볼까요?");
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
        assertThat(response.suggestedAction()).isEqualTo("3분만 해볼까요? 작게 시작하면 돼요.");
        assertThat(response.suggestedDurationMinutes()).isEqualTo(3);
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
        assertThat(response.postExamMode()).isFalse();
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

    @Test
    @DisplayName("query returns LONG_ABSENCE when last completed session was 7 days ago or earlier")
    void query_longAbsence_returnsLongAbsence() {
        givenCurrentUser();
        givenClock();
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(focusSessionRepository.findFirstByUserIdAndEndedAtIsNotNullOrderByEndedAtDesc(userId))
                .willReturn(Optional.of(completedFocusSession(today.minusDays(8))));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.needsRecovery()).isTrue();
        assertThat(response.recoveryType()).isEqualTo(RecoveryType.LONG_ABSENCE);
        assertThat(response.suggestedAction()).isEqualTo("오랜만이어도 괜찮아요. 오늘은 3분만 다시 연결해볼까요?");
        assertThat(response.daysSinceLastSession()).isEqualTo(8L);
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.RECOVERY_CARD_VIEWED),
                argThat(properties ->
                        properties.get("recoveryType").equals(RecoveryType.LONG_ABSENCE.name())
                                && properties.get("daysSinceLastSession").equals("8")
                )
        );
    }

    @Test
    @DisplayName("query enables post exam mode for exams from D+1 to D+7")
    void query_recentPostExam_returnsPostExamMode() {
        givenCurrentUser();
        givenClock();
        ExamSchedule recentExam = examSchedule(today.minusDays(3));
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubFocusSummary(7200)));
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubTopPicksSummary(3, 3)));
        given(examScheduleRepository.findFirstByUserIdAndExamDateBetweenOrderByExamDateDesc(
                userId,
                today.minusDays(7),
                today.minusDays(1)
        )).willReturn(Optional.of(recentExam));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.needsRecovery()).isTrue();
        assertThat(response.recoveryType()).isEqualTo(RecoveryType.POST_EXAM_RECOVERY);
        assertThat(response.postExamMode()).isTrue();
        assertThat(response.recentExamScheduleId()).isEqualTo(recentExam.getExamScheduleId().id());
        assertThat(response.recentExamType()).isEqualTo(ExamType.NAESIN);
        assertThat(response.recentExamTitle()).isEqualTo("중간고사");
        assertThat(response.recentExamDate()).isEqualTo(today.minusDays(3));
        assertThat(response.recentExamSubject()).isEqualTo("수학");
        assertThat(response.daysSinceRecentExam()).isEqualTo(3L);
        assertThat(response.suggestedAction()).isEqualTo("시험 직후에는 회복이 먼저예요. 오늘은 10분만 가볍게 다시 시작해볼까요?");
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.RECOVERY_CARD_VIEWED),
                argThat(properties ->
                        properties.get("postExamMode").equals("true")
                                && properties.get("recentExamScheduleId").equals(recentExam.getExamScheduleId().id().toString())
                                && properties.get("daysSinceRecentExam").equals("3")
                )
        );
    }

    @Test
    @DisplayName("query uses GENTLE coaching tone when behavior profile coachingMode is GENTLE")
    void query_gentleCoachingMode_usesGentleMessage() {
        givenCurrentUser();
        givenClock();
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfileWith(RecoveryStyle.SLOW_REBUILDER, CoachingMode.GENTLE)));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.suggestedAction())
                .isEqualTo("오늘은 3분이면 충분해요. 천천히 다시 연결해볼까요?");
        assertThat(response.suggestedDurationMinutes()).isEqualTo(3);
    }

    @Test
    @DisplayName("query uses STRICT coaching tone when behavior profile coachingMode is STRICT")
    void query_strictCoachingMode_usesStrictMessage() {
        givenCurrentUser();
        givenClock();
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.empty());
        given(behaviorProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(behaviorProfileWith(RecoveryStyle.QUICK_RESTART, CoachingMode.STRICT)));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.suggestedAction()).isEqualTo("준비됐어? 지금 바로 5분만 가자.");
        assertThat(response.suggestedDurationMinutes()).isEqualTo(5);
    }

    @Test
    @DisplayName("query returns suggestedDurationMinutes 10 for POST_EXAM_RECOVERY")
    void query_postExamRecovery_returnsTenMinutes() {
        givenCurrentUser();
        givenClock();
        ExamSchedule recentExam = examSchedule(today.minusDays(2));
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubFocusSummary(3600)));
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubTopPicksSummary(2, 2)));
        given(examScheduleRepository.findFirstByUserIdAndExamDateBetweenOrderByExamDateDesc(
                userId,
                today.minusDays(7),
                today.minusDays(1)
        )).willReturn(Optional.of(recentExam));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.recoveryType()).isEqualTo(RecoveryType.POST_EXAM_RECOVERY);
        assertThat(response.suggestedDurationMinutes()).isEqualTo(10);
    }

    @Test
    @DisplayName("query returns null suggestedDurationMinutes when recovery is not needed")
    void query_normalDay_returnsNullDuration() {
        givenCurrentUser();
        givenClock();
        given(dailyFocusSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubFocusSummary(7200)));
        given(dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, yesterday))
                .willReturn(Optional.of(stubTopPicksSummary(3, 3)));

        RecoveryCardResponse response = queryRecoveryCardService.query();

        assertThat(response.recoveryType()).isEqualTo(RecoveryType.NONE);
        assertThat(response.suggestedDurationMinutes()).isNull();
    }
}
