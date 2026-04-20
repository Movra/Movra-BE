package com.example.movra.application.planning.daily_plan;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.DailyTopPicksSummarySaver;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import com.example.movra.sharedkernel.persistence.RequiresNewInsertExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({DailyTopPicksSummarySaver.class, RequiresNewInsertExecutor.class})
class DailyTopPicksSummarySaverIntegrationTest {

    @Autowired
    private DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;

    @Autowired
    private DailyTopPicksSummarySaver dailyTopPicksSummarySaver;

    @AfterEach
    void tearDown() {
        dailyTopPicksSummaryRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void execute_duplicateKeyViolation_returnsFalseWithoutBreakingSubsequentOperations() {
        UserId userId = UserId.newId();
        LocalDate date = LocalDate.of(2026, 4, 14);
        Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        DailyPlan dailyPlan = DailyPlan.create(userId, date);
        var task = dailyPlan.addTask("Top Pick Task");
        dailyPlan.markAsTopPicked(task.getTaskId(), 30, "Memo");

        DailyTopPicksSummary first = DailyTopPicksSummary.close(dailyPlan, clock);
        DailyTopPicksSummary duplicate = DailyTopPicksSummary.close(dailyPlan, clock);

        assertThat(dailyTopPicksSummarySaver.save(first)).isTrue();
        assertThat(dailyTopPicksSummarySaver.save(duplicate)).isFalse();
        assertThat(dailyTopPicksSummaryRepository.count()).isEqualTo(1L);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void execute_dailyPlanConstraintViolation_throwsException() {
        UserId userId = UserId.newId();
        LocalDate date = LocalDate.of(2026, 4, 14);
        Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        DailyPlan dailyPlan = DailyPlan.create(userId, date);
        var task = dailyPlan.addTask("Top Pick Task");
        dailyPlan.markAsTopPicked(task.getTaskId(), 30, "Memo");

        DailyTopPicksSummary first = DailyTopPicksSummary.close(dailyPlan, clock);
        DailyTopPicksSummary conflicting = DailyTopPicksSummary.close(dailyPlan, clock);
        ReflectionTestUtils.setField(conflicting, "date", date.plusDays(1));

        assertThat(dailyTopPicksSummarySaver.save(first)).isTrue();
        assertThatThrownBy(() -> dailyTopPicksSummarySaver.save(conflicting))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(dailyTopPicksSummaryRepository.count()).isEqualTo(1L);
    }
}
