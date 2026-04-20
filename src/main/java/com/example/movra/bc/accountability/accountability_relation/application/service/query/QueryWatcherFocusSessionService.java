package com.example.movra.bc.accountability.accountability_relation.application.service.query;

import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherAccountabilityRelationReader;
import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherSummaryDateRangeValidator;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.focus.focus_session.application.service.support.DailyFocusSummaryReader;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryWatcherFocusSessionService {

    private final WatcherAccountabilityRelationReader watcherAccountabilityRelationReader;
    private final WatcherSummaryDateRangeValidator watcherSummaryDateRangeValidator;
    private final DailyFocusSummaryReader dailyFocusSummaryReader;

    public Optional<DailyFocusSummaryView> query(LocalDate date) {
        AccountabilityRelation relation = watcherAccountabilityRelationReader.getCurrentWatcherRelation();
        relation.ensureMonitoringTargetAllowed(MonitoringTarget.FOCUS_SESSION);

        return dailyFocusSummaryReader.findOne(relation.getSubjectUserId(), date);
    }

    public List<DailyFocusSummaryView> queryRange(LocalDate from, LocalDate to) {
        AccountabilityRelation relation = watcherAccountabilityRelationReader.getCurrentWatcherRelation();
        relation.ensureMonitoringTargetAllowed(MonitoringTarget.FOCUS_SESSION);
        watcherSummaryDateRangeValidator.validate(from, to);

        return dailyFocusSummaryReader.findRange(relation.getSubjectUserId(), from, to);
    }
}
