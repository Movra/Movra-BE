package com.example.movra.bc.accountability.accountability_relation.application.service.query;

import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherAccountabilityRelationReader;
import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherMonitoringContentReader;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityRelationResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.WatcherOverviewResponse;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryView;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryView;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryWatcherOverviewService {

    private final WatcherAccountabilityRelationReader watcherAccountabilityRelationReader;
    private final WatcherMonitoringContentReader watcherMonitoringContentReader;

    public WatcherOverviewResponse query(LocalDate date) {
        AccountabilityRelation relation = watcherAccountabilityRelationReader.getCurrentWatcherRelation();

        DailyFocusSummaryView focusSessions = relation.getVisibilityPolicy().allows(MonitoringTarget.FOCUS_SESSION)
                ? watcherMonitoringContentReader.findFocusSessions(relation.getSubjectUserId(), date).orElse(null)
                : null;
        DailyTopPicksSummaryView topPicks = relation.getVisibilityPolicy().allows(MonitoringTarget.TOP_PICKS)
                ? watcherMonitoringContentReader.findTopPicks(relation.getSubjectUserId(), date).orElse(null)
                : null;
        DailyTimetableSummaryView timetableTasks = relation.getVisibilityPolicy().allows(MonitoringTarget.TIMETABLE_TASK)
                ? watcherMonitoringContentReader.findTimetableTasks(relation.getSubjectUserId(), date).orElse(null)
                : null;

        return new WatcherOverviewResponse(
                FriendAccountabilityRelationResponse.from(relation),
                date,
                focusSessions,
                topPicks,
                timetableTasks
        );
    }
}
