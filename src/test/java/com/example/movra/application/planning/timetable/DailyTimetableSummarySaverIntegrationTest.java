package com.example.movra.application.planning.timetable;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.timetable.application.service.support.DailyTimetableSummarySaver;
import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.repository.DailyTimetableSummaryRepository;
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
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({DailyTimetableSummarySaver.class, RequiresNewInsertExecutor.class})
class DailyTimetableSummarySaverIntegrationTest {

    @Autowired
    private DailyTimetableSummaryRepository dailyTimetableSummaryRepository;

    @Autowired
    private DailyTimetableSummarySaver dailyTimetableSummarySaver;

    @AfterEach
    void tearDown() {
        dailyTimetableSummaryRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_duplicateKeyViolation_returnsFalseWithoutBreakingSubsequentOperations() {
        UserId userId = UserId.newId();
        LocalDate date = LocalDate.of(2026, 4, 14);
        Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        DailyPlan dailyPlan = createDailyPlan(userId, date);
        Timetable timetable = createTimetable(dailyPlan);

        DailyTimetableSummary first = DailyTimetableSummary.close(dailyPlan, timetable, clock);
        DailyTimetableSummary duplicate = DailyTimetableSummary.close(dailyPlan, timetable, clock);

        assertThat(dailyTimetableSummarySaver.save(first)).isTrue();
        assertThat(dailyTimetableSummarySaver.save(duplicate)).isFalse();
        assertThat(dailyTimetableSummaryRepository.count()).isEqualTo(1L);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_dailyPlanConstraintViolation_throwsException() {
        UserId userId = UserId.newId();
        LocalDate date = LocalDate.of(2026, 4, 14);
        Clock clock = Clock.fixed(Instant.parse("2026-04-15T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        DailyPlan dailyPlan = createDailyPlan(userId, date);
        Timetable timetable = createTimetable(dailyPlan);

        DailyTimetableSummary first = DailyTimetableSummary.close(dailyPlan, timetable, clock);
        DailyTimetableSummary conflicting = DailyTimetableSummary.close(dailyPlan, timetable, clock);
        ReflectionTestUtils.setField(conflicting, "date", date.plusDays(1));

        assertThat(dailyTimetableSummarySaver.save(first)).isTrue();
        assertThatThrownBy(() -> dailyTimetableSummarySaver.save(conflicting))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(dailyTimetableSummaryRepository.count()).isEqualTo(1L);
    }

    private DailyPlan createDailyPlan(UserId userId, LocalDate date) {
        DailyPlan dailyPlan = DailyPlan.create(userId, date);
        var topPickTask = dailyPlan.addTask("Top Pick Task");
        var generalTask = dailyPlan.addTask("General Task");
        dailyPlan.markAsTopPicked(topPickTask.getTaskId(), 60, "Deep work", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        dailyPlan.completeTask(topPickTask.getTaskId());
        dailyPlan.completeTask(generalTask.getTaskId());
        return dailyPlan;
    }

    private Timetable createTimetable(DailyPlan dailyPlan) {
        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 1);
        var topPickTask = dailyPlan.getTasks().get(0);
        var generalTask = dailyPlan.getTasks().get(1);
        timetable.assignTopPick(topPickTask.getTaskId(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        timetable.assignTask(generalTask.getTaskId(), LocalTime.of(11, 0), LocalTime.of(12, 0));
        return timetable;
    }
}
