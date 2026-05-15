package com.example.movra.application.home.today;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.bc.analytics.activation_funnel.domain.ActivationFunnel;
import com.example.movra.bc.analytics.activation_funnel.domain.repository.ActivationFunnelRepository;
import com.example.movra.bc.home.today.application.service.QueryHomeTodayService;
import com.example.movra.bc.home.today.application.service.dto.response.HomeTodayResponse;
import com.example.movra.bc.notification.application.service.QueryNotificationPreferenceService;
import com.example.movra.bc.notification.application.service.dto.response.NotificationPreferenceResponse;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.QueryTodayPlanningOverviewService;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response.TodayPlanningOverviewResponse;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.response.TopPicksResponse;
import com.example.movra.bc.planning.exam_schedule.application.service.QueryExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.ExamScheduleResponse;
import com.example.movra.bc.planning.exam_schedule.domain.type.ExamType;
import com.example.movra.bc.planning.exam_schedule.domain.type.SeasonMode;
import com.example.movra.bc.visioning.future_vision.application.service.QueryFutureVisionService;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.RecordComponent;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QueryHomeTodayServiceTest {

    @Mock
    private QueryTodayPlanningOverviewService queryTodayPlanningOverviewService;

    @Mock
    private QueryFutureVisionService queryFutureVisionService;

    @Mock
    private QueryExamScheduleService queryExamScheduleService;

    @Mock
    private QueryNotificationPreferenceService queryNotificationPreferenceService;

    @Mock
    private AccountabilityRelationRepository accountabilityRelationRepository;

    @Mock
    private ActivationFunnelRepository activationFunnelRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-04-29T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final UserId userId = UserId.newId();
    private final LocalDate today = LocalDate.of(2026, 4, 29);
    private final UUID dailyPlanId = UUID.randomUUID();

    private QueryHomeTodayService queryHomeTodayService;

    @BeforeEach
    void setUp() {
        queryHomeTodayService = new QueryHomeTodayService(
                queryTodayPlanningOverviewService,
                queryFutureVisionService,
                queryExamScheduleService,
                queryNotificationPreferenceService,
                accountabilityRelationRepository,
                activationFunnelRepository,
                currentUserQuery,
                clock
        );
    }

    @Test
    @DisplayName("home today response exposes only the summary fields required by the home screen")
    void homeTodayResponse_contract_containsOnlyHomeSummaryFields() {
        assertThat(HomeTodayResponse.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactly(
                        "targetDate",
                        "futureVision",
                        "topPicks",
                        "timetable",
                        "seasonMode",
                        "nextExamSchedule",
                        "notificationPreference",
                        "friendAccountability",
                        "showFocusTimingCard"
                )
                .doesNotContain(
                        "todayDailyPlan",
                        "morningTasks",
                        "focusSessions",
                        "activeFocusSession",
                        "recoveryCard",
                        "behaviorProfile"
                );
    }

    @Test
    @DisplayName("query builds today's home payload and tolerates optional missing setup")
    void query_missingOptionalSetup_returnsHomePayload() {
        givenBaseHomeData(List.of());
        given(queryFutureVisionService.findForHome()).willReturn(Optional.empty());
        given(queryExamScheduleService.findNextForHome()).willReturn(Optional.empty());
        given(activationFunnelRepository.findByUserId(userId)).willReturn(Optional.empty());

        HomeTodayResponse response = queryHomeTodayService.query();

        assertThat(response.targetDate()).isEqualTo(today);
        assertThat(response.futureVision()).isNull();
        assertThat(response.topPicks()).hasSize(1);
        assertThat(response.timetable()).isNull();
        assertThat(response.seasonMode()).isEqualTo(SeasonMode.BASELINE_MODE);
        assertThat(response.nextExamSchedule()).isNull();
        assertThat(response.notificationPreference()).isEqualTo(notificationPreferenceResponse());
        assertThat(response.friendAccountability().relationCreated()).isFalse();
        assertThat(response.friendAccountability().watchedByFriend()).isFalse();
        assertThat(response.friendAccountability().watchingFriend()).isFalse();
        assertThat(response.showFocusTimingCard()).isFalse();
    }

    @Test
    @DisplayName("query includes friend accountability status")
    void query_withFriendAccountability_returnsStatus() {
        UserId friendUserId = UserId.newId();
        AccountabilityRelation subjectRelation = relationWatchedBy(friendUserId);
        AccountabilityRelation watcherRelation = relationWatchedBy(userId, friendUserId);

        givenBaseHomeData(List.of(subjectRelation, watcherRelation));
        given(activationFunnelRepository.findByUserId(userId)).willReturn(Optional.empty());

        HomeTodayResponse response = queryHomeTodayService.query();

        assertThat(response.friendAccountability().relationCreated()).isTrue();
        assertThat(response.friendAccountability().watchedByFriend()).isTrue();
        assertThat(response.friendAccountability().watchingFriend()).isTrue();
        assertThat(response.friendAccountability().inviteCodeStatus()).isNotNull();
        assertThat(response.friendAccountability().inviteCodeStatus().watcherConnected()).isTrue();
    }

    @Test
    @DisplayName("query includes next exam schedule and notification preference")
    void query_withNextExamAndNotificationPreference_returnsHomePayload() {
        ExamScheduleResponse nextExamSchedule = ExamScheduleResponse.builder()
                .examScheduleId(UUID.randomUUID())
                .examType(ExamType.NAESIN)
                .title("Midterm")
                .examDate(today.plusDays(7))
                .subject("Math")
                .daysUntil(7)
                .seasonMode(SeasonMode.NAESIN_INTENSIVE)
                .build();

        givenBaseHomeData(List.of());
        given(queryFutureVisionService.findForHome()).willReturn(Optional.empty());
        given(queryExamScheduleService.findNextForHome()).willReturn(Optional.of(nextExamSchedule));
        given(activationFunnelRepository.findByUserId(userId)).willReturn(Optional.empty());

        HomeTodayResponse response = queryHomeTodayService.query();

        assertThat(response.nextExamSchedule()).isEqualTo(nextExamSchedule);
        assertThat(response.seasonMode()).isEqualTo(SeasonMode.NAESIN_INTENSIVE);
        assertThat(response.notificationPreference()).isEqualTo(notificationPreferenceResponse());
    }

    @Test
    @DisplayName("query sets showFocusTimingCard true once seven days have passed since signup")
    void query_sevenDaysSinceSignup_signalsFocusTimingCard() {
        ActivationFunnel funnel = ActivationFunnel.create(userId);
        funnel.markSignup(clock.instant().minus(java.time.Duration.ofDays(8)), "instagram", "H2");

        givenBaseHomeData(List.of());
        given(activationFunnelRepository.findByUserId(userId)).willReturn(Optional.of(funnel));

        HomeTodayResponse response = queryHomeTodayService.query();

        assertThat(response.showFocusTimingCard()).isTrue();
    }

    @Test
    @DisplayName("query keeps showFocusTimingCard false when signup is fresher than seven days")
    void query_lessThanSevenDays_keepsFocusTimingCardFalse() {
        ActivationFunnel funnel = ActivationFunnel.create(userId);
        funnel.markSignup(clock.instant().minus(java.time.Duration.ofDays(3)), "youtube", "H3");

        givenBaseHomeData(List.of());
        given(activationFunnelRepository.findByUserId(userId)).willReturn(Optional.of(funnel));

        HomeTodayResponse response = queryHomeTodayService.query();

        assertThat(response.showFocusTimingCard()).isFalse();
    }

    private void givenBaseHomeData(List<AccountabilityRelation> accountabilityRelations) {
        given(queryTodayPlanningOverviewService.query()).willReturn(todayPlanningOverviewResponse());
        given(queryNotificationPreferenceService.queryMine()).willReturn(notificationPreferenceResponse());
        given(currentUserQuery.currentUser()).willReturn(AuthenticatedUser.builder().userId(userId).build());
        given(accountabilityRelationRepository.findAllBySubjectUserIdOrWatcherUserId(userId, userId))
                .willReturn(accountabilityRelations);
    }

    private TodayPlanningOverviewResponse todayPlanningOverviewResponse() {
        TopPicksResponse topPick = TopPicksResponse.builder()
                .taskId(UUID.randomUUID())
                .content("Math workbook")
                .completed(false)
                .estimatedMinutes(30)
                .memo("Chapter 3")
                .build();

        return TodayPlanningOverviewResponse.builder()
                .dailyPlanId(dailyPlanId)
                .targetDate(today)
                .topPicks(List.of(topPick))
                .timetable(null)
                .build();
    }

    private NotificationPreferenceResponse notificationPreferenceResponse() {
        return NotificationPreferenceResponse.builder()
                .notificationPreferenceId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .dailyFocusEnabled(false)
                .dailyTopPicksEnabled(false)
                .dailyTimetableEnabled(false)
                .accountabilityEnabled(false)
                .schoolHoursQuietEnabled(true)
                .schoolHoursStart(java.time.LocalTime.of(8, 0))
                .schoolHoursEnd(java.time.LocalTime.of(15, 30))
                .weekendSchoolQuietEnabled(false)
                .sleepHoursQuietEnabled(true)
                .maxDailyPushCount(3)
                .build();
    }

    private AccountabilityRelation relationWatchedBy(UserId watcherUserId) {
        return relationWatchedBy(watcherUserId, userId);
    }

    private AccountabilityRelation relationWatchedBy(UserId watcherUserId, UserId subjectUserId) {
        AccountabilityRelation relation = AccountabilityRelation.create(
                subjectUserId,
                new VisibilityPolicy(Set.of(MonitoringTarget.FOCUS_SESSION)),
                clock
        );
        relation.joinByInviteCode(relation.getInviteCode().code(), watcherUserId, clock);
        return relation;
    }
}
