package com.example.movra.bc.home.today.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.InviteCodeStatusResponse;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.repository.AccountabilityRelationRepository;
import com.example.movra.bc.analytics.activation_funnel.domain.repository.ActivationFunnelRepository;
import com.example.movra.bc.home.today.application.service.dto.response.FriendAccountabilityStatusResponse;
import com.example.movra.bc.home.today.application.service.dto.response.HomeTodayResponse;
import com.example.movra.bc.notification.application.service.QueryNotificationPreferenceService;
import com.example.movra.bc.notification.application.service.dto.response.NotificationPreferenceResponse;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.QueryTodayPlanningOverviewService;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response.TodayPlanningOverviewResponse;
import com.example.movra.bc.planning.exam_schedule.application.service.QueryExamScheduleService;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.ExamScheduleResponse;
import com.example.movra.bc.planning.exam_schedule.domain.type.SeasonMode;
import com.example.movra.bc.visioning.future_vision.application.service.QueryFutureVisionService;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.FutureVisionResponse;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryHomeTodayService {

    private final QueryTodayPlanningOverviewService queryTodayPlanningOverviewService;
    private final QueryFutureVisionService queryFutureVisionService;
    private final QueryExamScheduleService queryExamScheduleService;
    private final QueryNotificationPreferenceService queryNotificationPreferenceService;
    private final AccountabilityRelationRepository accountabilityRelationRepository;
    private final ActivationFunnelRepository activationFunnelRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    public HomeTodayResponse query() {
        TodayPlanningOverviewResponse todayPlanningOverview = queryTodayPlanningOverviewService.query();
        ExamScheduleResponse nextExamSchedule = queryNextExamSchedule();

        return HomeTodayResponse.builder()
                .targetDate(todayPlanningOverview.targetDate())
                .futureVision(queryFutureVision())
                .topPicks(todayPlanningOverview.topPicks())
                .timetable(todayPlanningOverview.timetable())
                .seasonMode(seasonMode(nextExamSchedule))
                .nextExamSchedule(nextExamSchedule)
                .notificationPreference(queryNotificationPreference())
                .friendAccountability(queryFriendAccountabilityStatus())
                .showFocusTimingCard(showFocusTimingCard())
                .build();
    }

    private boolean showFocusTimingCard() {
        UserId userId = currentUserQuery.currentUser().userId();
        return activationFunnelRepository.findByUserId(userId)
                .map(activationFunnel -> activationFunnel.isFocusTimingCardAvailable(clock))
                .orElse(false);
    }

    private FutureVisionResponse queryFutureVision() {
        return queryFutureVisionService.findForHome().orElse(null);
    }

    private ExamScheduleResponse queryNextExamSchedule() {
        return queryExamScheduleService.findNextForHome().orElse(null);
    }

    private SeasonMode seasonMode(ExamScheduleResponse nextExamSchedule) {
        if (nextExamSchedule == null || nextExamSchedule.seasonMode() == null) {
            return SeasonMode.BASELINE_MODE;
        }
        return nextExamSchedule.seasonMode();
    }

    private NotificationPreferenceResponse queryNotificationPreference() {
        return queryNotificationPreferenceService.queryMine();
    }

    private FriendAccountabilityStatusResponse queryFriendAccountabilityStatus() {
        UserId userId = currentUserQuery.currentUser().userId();

        List<AccountabilityRelation> relations =
                accountabilityRelationRepository.findAllBySubjectUserIdOrWatcherUserId(userId, userId);

        AccountabilityRelation subjectRelation = null;
        AccountabilityRelation watcherRelation = null;
        for (AccountabilityRelation relation : relations) {
            if (subjectRelation == null && userId.equals(relation.getSubjectUserId())) {
                subjectRelation = relation;
            }
            if (watcherRelation == null && userId.equals(relation.getWatcherUserId())) {
                watcherRelation = relation;
            }
            if (subjectRelation != null && watcherRelation != null) {
                break;
            }
        }

        InviteCodeStatusResponse inviteCodeStatus = subjectRelation != null
                ? InviteCodeStatusResponse.from(subjectRelation, clock)
                : null;

        return FriendAccountabilityStatusResponse.builder()
                .relationCreated(subjectRelation != null)
                .watchedByFriend(subjectRelation != null && subjectRelation.getWatcherUserId() != null)
                .watchingFriend(watcherRelation != null)
                .inviteCodeStatus(inviteCodeStatus)
                .build();
    }
}
