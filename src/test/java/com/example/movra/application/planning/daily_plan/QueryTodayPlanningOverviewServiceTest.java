package com.example.movra.application.planning.daily_plan;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.QueryTodayPlanningOverviewService;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.TodayDailyPlanProvisioner;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response.TodayPlanningOverviewResponse;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QueryTodayPlanningOverviewServiceTest {

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private TodayDailyPlanProvisioner todayDailyPlanProvisioner;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-04-29T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final UserId userId = UserId.newId();
    private final LocalDate today = LocalDate.of(2026, 4, 29);

    private QueryTodayPlanningOverviewService queryTodayPlanningOverviewService;

    @BeforeEach
    void setUp() {
        queryTodayPlanningOverviewService = new QueryTodayPlanningOverviewService(
                dailyPlanRepository,
                timetableRepository,
                todayDailyPlanProvisioner,
                currentUserQuery,
                clock
        );
    }

    @Test
    @DisplayName("query builds planning overview from fetched daily plan and timetable")
    void query_existingDailyPlanWithTimetable_returnsOverview() {
        DailyPlan dailyPlan = DailyPlan.create(userId, today);
        Task topPick = dailyPlan.addTask("Math workbook");
        dailyPlan.markAsTopPicked(topPick.getTaskId(), 30, "Chapter 3", DailyPlan.DEFAULT_MAX_TOP_PICKS);
        dailyPlan.addMorningTask("Pack bag");

        Timetable timetable = Timetable.create(dailyPlan.getDailyPlanId(), 1);
        timetable.assignTopPick(topPick.getTaskId(), LocalTime.of(9, 0), LocalTime.of(9, 30));

        givenCurrentUser();
        given(dailyPlanRepository.findByUserIdAndPlanDateWithTasks(userId, today))
                .willReturn(Optional.of(dailyPlan));
        given(timetableRepository.findByDailyPlanIdWithSlots(dailyPlan.getDailyPlanId()))
                .willReturn(Optional.of(timetable));

        TodayPlanningOverviewResponse response = queryTodayPlanningOverviewService.query();

        assertThat(response.dailyPlanId()).isEqualTo(dailyPlan.getDailyPlanId().id());
        assertThat(response.targetDate()).isEqualTo(today);
        assertThat(response.topPicks()).hasSize(1);
        assertThat(response.topPicks().get(0).taskId()).isEqualTo(topPick.getTaskId().id());
        assertThat(response.topPicks().get(0).estimatedMinutes()).isEqualTo(30);
        assertThat(response.timetable()).isNotNull();
        assertThat(response.timetable().slots()).hasSize(1);
        assertThat(response.timetable().slots().get(0).content()).isEqualTo("Math workbook");
        then(dailyPlanRepository).should().findByUserIdAndPlanDateWithTasks(userId, today);
        then(timetableRepository).should().findByDailyPlanIdWithSlots(dailyPlan.getDailyPlanId());
    }

    @Test
    @DisplayName("query delegates to provisioner when today's plan is missing")
    void query_missingDailyPlan_createsToday() {
        DailyPlan newPlan = DailyPlan.create(userId, today);

        givenCurrentUser();
        given(dailyPlanRepository.findByUserIdAndPlanDateWithTasks(userId, today))
                .willReturn(Optional.empty());
        given(todayDailyPlanProvisioner.createOrLoadToday(userId, today))
                .willReturn(newPlan);
        given(timetableRepository.findByDailyPlanIdWithSlots(any(DailyPlanId.class)))
                .willReturn(Optional.empty());

        TodayPlanningOverviewResponse response = queryTodayPlanningOverviewService.query();

        assertThat(response.dailyPlanId()).isEqualTo(newPlan.getDailyPlanId().id());
        assertThat(response.targetDate()).isEqualTo(today);
        assertThat(response.topPicks()).isEmpty();
        assertThat(response.timetable()).isNull();
        then(todayDailyPlanProvisioner).should().createOrLoadToday(userId, today);
    }

    private void givenCurrentUser() {
        given(currentUserQuery.currentUser())
                .willReturn(AuthenticatedUser.builder().userId(userId).build());
    }
}
