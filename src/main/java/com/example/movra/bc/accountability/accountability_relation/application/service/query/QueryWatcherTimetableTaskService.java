package com.example.movra.bc.accountability.accountability_relation.application.service.query;

import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherAccountabilityRelationReader;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.planning.timetable.application.service.support.DailyTimetableSummaryReader;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryWatcherTimetableTaskService {

    private final WatcherAccountabilityRelationReader watcherAccountabilityRelationReader;
    private final DailyTimetableSummaryReader dailyTimetableSummaryReader;

    public Optional<DailyTimetableSummaryView> query(LocalDate date) {
        AccountabilityRelation relation = watcherAccountabilityRelationReader.getCurrentWatcherRelation();
        relation.ensureMonitoringTargetAllowed(MonitoringTarget.TIMETABLE_TASK);

        return dailyTimetableSummaryReader.findOne(relation.getSubjectUserId(), date);
    }

    public List<DailyTimetableSummaryView> queryRange(LocalDate from, LocalDate to) {
        AccountabilityRelation relation = watcherAccountabilityRelationReader.getCurrentWatcherRelation();
        relation.ensureMonitoringTargetAllowed(MonitoringTarget.TIMETABLE_TASK);

        return dailyTimetableSummaryReader.findRange(relation.getSubjectUserId(), from, to);
    }
}
