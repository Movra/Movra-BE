package com.example.movra.application.planning.daily_plan;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.DailyTopPicksCloser;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.DailyTopPicksSummarySaver;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
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
import java.util.Comparator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CloseDailyTopPicksServiceTest {

    private DailyTopPicksCloser closeDailyTopPicksService;

    @Mock
    private DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private DailyTopPicksSummarySaver dailyTopPicksSummarySaver;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final UserId userId = UserId.newId();
    private final LocalDate date = LocalDate.of(2026, 4, 14);

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        closeDailyTopPicksService = new DailyTopPicksCloser(
                dailyTopPicksSummaryRepository, dailyPlanRepository, dailyTopPicksSummarySaver, clock
        );
    }

    @Test
    @DisplayName("close counts top-picked tasks and completed ones")
    void close_aggregatesTopPicks() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        given(dailyTopPicksSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, date)).willReturn(Optional.of(dailyPlan));
        given(dailyTopPicksSummarySaver.save(any())).willReturn(true);

        // when
        closeDailyTopPicksService.close(userId, date);

        // then
        ArgumentCaptor<DailyTopPicksSummary> captor = ArgumentCaptor.forClass(DailyTopPicksSummary.class);
        verify(dailyTopPicksSummarySaver).save(captor.capture());
        DailyTopPicksSummary saved = captor.getValue();
        var expectedTopPicks = dailyPlan.getTasks().stream()
                .filter(task -> task.isTopPicked())
                .sorted(Comparator.comparing(task -> task.getTaskId().id()))
                .toList();
        assertThat(saved.getDailyPlanId()).isEqualTo(dailyPlan.getDailyPlanId());
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getTotalCount()).isEqualTo(2);
        assertThat(saved.getCompletedCount()).isEqualTo(1);
        assertThat(saved.getItems()).hasSize(2);
        for (int i = 0; i < expectedTopPicks.size(); i++) {
            var expected = expectedTopPicks.get(i);
            var actual = saved.getItems().get(i);
            assertThat(actual.getOriginalTaskId()).isEqualTo(expected.getTaskId());
            assertThat(actual.getContentSnapshot()).isEqualTo(expected.getContent());
            assertThat(actual.isCompletedSnapshot()).isEqualTo(expected.isCompleted());
            assertThat(actual.getEstimatedMinutesSnapshot()).isEqualTo(expected.getTopPickDetail().getEstimatedMinutes());
            assertThat(actual.getMemoSnapshot()).isEqualTo(expected.getTopPickDetail().getMemo());
            assertThat(actual.getDisplayOrder()).isEqualTo(i + 1);
        }
    }

    @Test
    @DisplayName("close is idempotent when a summary already exists")
    void close_idempotent() {
        // given
        given(dailyTopPicksSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(true);

        // when
        closeDailyTopPicksService.close(userId, date);

        // then
        verify(dailyTopPicksSummarySaver, never()).save(any());
    }

    @Test
    @DisplayName("close skips when there is no daily plan")
    void close_noPlan() {
        // given
        given(dailyTopPicksSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, date)).willReturn(Optional.empty());

        // when
        closeDailyTopPicksService.close(userId, date);

        // then
        verify(dailyTopPicksSummarySaver, never()).save(any());
    }

    @Test
    @DisplayName("close treats duplicate writes as idempotent success")
    void close_duplicateAtWrite_isIgnored() {
        // given
        DailyPlan dailyPlan = createDailyPlan();
        given(dailyTopPicksSummaryRepository.existsByUserIdAndDate(userId, date)).willReturn(false);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, date)).willReturn(Optional.of(dailyPlan));
        given(dailyTopPicksSummarySaver.save(any())).willReturn(false);

        // when / then
        assertThatCode(() -> closeDailyTopPicksService.close(userId, date))
                .doesNotThrowAnyException();
    }

    private DailyPlan createDailyPlan() {
        DailyPlan dailyPlan = DailyPlan.create(userId, date);
        var topPickDone = dailyPlan.addTask("Top Pick Done");
        var topPickPending = dailyPlan.addTask("Top Pick Pending");
        dailyPlan.addTask("General Task");
        dailyPlan.markAsTopPicked(topPickDone.getTaskId(), 30, "Done memo", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        dailyPlan.markAsTopPicked(topPickPending.getTaskId(), 45, "Pending memo", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        dailyPlan.completeTask(topPickDone.getTaskId());
        return dailyPlan;
    }
}
