package com.example.movra.application.planning.daily_plan;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.CloseDailyTopPicksService;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import com.example.movra.bc.planning.daily_plan.domain.type.ClosedBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CloseDailyTopPicksServiceTest {

    private CloseDailyTopPicksService closeDailyTopPicksService;

    @Mock
    private DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private DailyPlan dailyPlan;

    @Mock
    private Task topPickDone;

    @Mock
    private Task topPickPending;

    @Mock
    private Task generalTask;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final UserId userId = UserId.newId();
    private final LocalDate date = LocalDate.of(2026, 4, 14);

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        closeDailyTopPicksService = new CloseDailyTopPicksService(dailyTopPicksSummaryRepository, dailyPlanRepository, clock);
    }

    @Test
    @DisplayName("close counts top-picked tasks and completed ones")
    void close_aggregatesTopPicks() {
        // given
        given(dailyTopPicksSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, date)).willReturn(Optional.of(dailyPlan));

        given(topPickDone.isTopPicked()).willReturn(true);
        given(topPickDone.isCompleted()).willReturn(true);
        given(topPickPending.isTopPicked()).willReturn(true);
        given(topPickPending.isCompleted()).willReturn(false);
        given(generalTask.isTopPicked()).willReturn(false);

        given(dailyPlan.getTasks()).willReturn(List.of(topPickDone, topPickPending, generalTask));

        // when
        closeDailyTopPicksService.close(userId, date, ClosedBy.USER_ACTION);

        // then
        ArgumentCaptor<DailyTopPicksSummary> captor = ArgumentCaptor.forClass(DailyTopPicksSummary.class);
        verify(dailyTopPicksSummaryRepository).save(captor.capture());
        DailyTopPicksSummary saved = captor.getValue();
        assertThat(saved.getTotalCount()).isEqualTo(2);
        assertThat(saved.getCompletedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("close is idempotent when a summary already exists")
    void close_idempotent() {
        // given
        given(dailyTopPicksSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(true);

        // when
        closeDailyTopPicksService.close(userId, date, ClosedBy.USER_ACTION);

        // then
        verify(dailyTopPicksSummaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("close records zero counts when there is no daily plan")
    void close_noPlan() {
        // given
        given(dailyTopPicksSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, date)).willReturn(Optional.empty());

        // when
        closeDailyTopPicksService.close(userId, date, ClosedBy.SCHEDULER);

        // then
        ArgumentCaptor<DailyTopPicksSummary> captor = ArgumentCaptor.forClass(DailyTopPicksSummary.class);
        verify(dailyTopPicksSummaryRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalCount()).isZero();
        assertThat(captor.getValue().getCompletedCount()).isZero();
    }
}
