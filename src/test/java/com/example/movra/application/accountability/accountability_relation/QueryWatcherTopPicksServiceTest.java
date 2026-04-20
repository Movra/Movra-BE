package com.example.movra.application.accountability.accountability_relation;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherAccountabilityRelationReader;
import com.example.movra.bc.accountability.accountability_relation.application.helper.WatcherSummaryDateRangeValidator;
import com.example.movra.bc.accountability.accountability_relation.application.service.exception.InvalidDateRangeException;
import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryWatcherTopPicksService;
import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.VisibilityPolicy;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.DailyTopPicksReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class QueryWatcherTopPicksServiceTest {

    @Mock
    private WatcherAccountabilityRelationReader watcherAccountabilityRelationReader;

    @Mock
    private DailyTopPicksReader dailyTopPicksReader;

    private QueryWatcherTopPicksService queryWatcherTopPicksService;

    @BeforeEach
    void setUp() {
        queryWatcherTopPicksService = new QueryWatcherTopPicksService(
                watcherAccountabilityRelationReader,
                new WatcherSummaryDateRangeValidator(),
                dailyTopPicksReader
        );
    }

    @Test
    void queryRange_fromAfterTo_throwsException() {
        given(watcherAccountabilityRelationReader.getCurrentWatcherRelation())
                .willReturn(relationWith(MonitoringTarget.TOP_PICKS));

        assertThatThrownBy(() -> queryWatcherTopPicksService.queryRange(
                LocalDate.of(2026, 4, 15),
                LocalDate.of(2026, 4, 14)
        )).isInstanceOf(InvalidDateRangeException.class);

        verifyNoInteractions(dailyTopPicksReader);
    }

    private AccountabilityRelation relationWith(MonitoringTarget target) {
        return AccountabilityRelation.create(
                UserId.newId(),
                new VisibilityPolicy(Set.of(target)),
                Clock.fixed(Instant.parse("2026-04-20T00:00:00Z"), ZoneId.of("Asia/Seoul"))
        );
    }
}
