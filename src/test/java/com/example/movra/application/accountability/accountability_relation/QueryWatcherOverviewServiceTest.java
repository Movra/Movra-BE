package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherAccountabilityRelationReader;
import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherMonitoringContentReader;
import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryWatcherOverviewService;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryView;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class QueryWatcherOverviewServiceTest {

    @Mock
    private WatcherAccountabilityRelationReader watcherAccountabilityRelationReader;

    @Mock
    private WatcherMonitoringContentReader watcherMonitoringContentReader;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-04-20T00:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final UserId subjectUserId = UserId.newId();
    private final UserId watcherUserId = UserId.newId();
    private final LocalDate date = LocalDate.of(2026, 4, 20);

    private QueryWatcherOverviewService queryWatcherOverviewService;

    @BeforeEach
    void setUp() {
        queryWatcherOverviewService = new QueryWatcherOverviewService(
                watcherAccountabilityRelationReader,
                watcherMonitoringContentReader
        );
    }

    @Test
    @DisplayName("query returns only allowed monitoring contents")
    void query_returnsOnlyAllowedContents() {
        AccountabilityRelation relation = relationWith(MonitoringTarget.TOP_PICKS);
        DailyTopPicksSummaryView topPicks = new DailyTopPicksSummaryView(
                subjectUserId, date, 0, 0, List.of()
        );
        given(watcherAccountabilityRelationReader.getCurrentWatcherRelation()).willReturn(relation);
        given(watcherMonitoringContentReader.findTopPicks(subjectUserId, date)).willReturn(Optional.of(topPicks));

        var response = queryWatcherOverviewService.query(date);

        assertThat(response.relation().subjectUserId()).isEqualTo(subjectUserId.id());
        assertThat(response.relation().watcherUserId()).isEqualTo(watcherUserId.id());
        assertThat(response.relation().allowedTargets()).containsExactly(MonitoringTarget.TOP_PICKS);
        assertThat(response.topPicks()).isSameAs(topPicks);
        assertThat(response.focusSessions()).isNull();
        assertThat(response.timetableTasks()).isNull();
        then(watcherMonitoringContentReader).should(never()).findFocusSessions(subjectUserId, date);
        then(watcherMonitoringContentReader).should(never()).findTimetableTasks(subjectUserId, date);
    }

    private AccountabilityRelation relationWith(MonitoringTarget target) {
        AccountabilityRelation relation = AccountabilityRelation.create(
                subjectUserId,
                new VisibilityPolicy(Set.of(target)),
                clock
        );
        relation.joinByInviteCode(relation.getInviteCode().code(), watcherUserId, clock);
        return relation;
    }
}
