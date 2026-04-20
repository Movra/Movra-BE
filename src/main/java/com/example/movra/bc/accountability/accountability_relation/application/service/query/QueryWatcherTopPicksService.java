package com.example.movra.bc.accountability.accountability_relation.application.service.query;

import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherAccountabilityRelationReader;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.DailyTopPicksReader;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryWatcherTopPicksService {

    private final WatcherAccountabilityRelationReader watcherAccountabilityRelationReader;
    private final DailyTopPicksReader dailyTopPicksReader;

    public Optional<DailyTopPicksSummaryView> query(LocalDate date) {
        AccountabilityRelation relation = watcherAccountabilityRelationReader.getCurrentWatcherRelation();
        relation.ensureMonitoringTargetAllowed(MonitoringTarget.TOP_PICKS);

        return dailyTopPicksReader.findOne(relation.getSubjectUserId(), date);
    }

    public List<DailyTopPicksSummaryView> queryRange(LocalDate from, LocalDate to) {
        AccountabilityRelation relation = watcherAccountabilityRelationReader.getCurrentWatcherRelation();
        relation.ensureMonitoringTargetAllowed(MonitoringTarget.TOP_PICKS);

        return dailyTopPicksReader.findRange(relation.getSubjectUserId(), from, to);
    }
}
