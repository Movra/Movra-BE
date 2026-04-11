package com.example.movra.application.planning.daily_plan;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.DailyPlanQueryService;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.response.DailyPlanResponse;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DailyPlanQueryServiceTest {

    @InjectMocks
    private DailyPlanQueryService dailyPlanQueryService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();
    private final LocalDate planDate = LocalDate.of(2026, 3, 17);

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    private DataIntegrityViolationException duplicateKeyViolation() {
        return new DataIntegrityViolationException(
                "duplicate",
                new SQLIntegrityConstraintViolationException("duplicate", "23000", 1062)
        );
    }

    private DataIntegrityViolationException otherIntegrityViolation() {
        return new DataIntegrityViolationException(
                "integrity",
                new SQLException("integrity", "23514", 23514)
        );
    }

    @Test
    @DisplayName("findByPlanDate succeeds")
    void findByPlanDate_success() {
        givenCurrentUser();
        DailyPlan dailyPlan = DailyPlan.create(userId, planDate);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, planDate)).willReturn(Optional.of(dailyPlan));

        DailyPlanResponse response = dailyPlanQueryService.findByPlanDate(planDate);

        assertThat(response.planDate()).isEqualTo(planDate);
        assertThat(response.tasks()).isEmpty();
        assertThat(response.morningTasks()).isEmpty();
    }

    @Test
    @DisplayName("findByPlanDate throws when missing")
    void findByPlanDate_notFound_throwsException() {
        givenCurrentUser();
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, planDate)).willReturn(Optional.empty());

        assertThatThrownBy(() -> dailyPlanQueryService.findByPlanDate(planDate))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }

    @Test
    @DisplayName("findOrCreateToday returns the existing plan")
    void findOrCreateToday_existing_success() {
        givenCurrentUser();
        LocalDate today = LocalDate.now();
        DailyPlan dailyPlan = DailyPlan.create(userId, today);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, today)).willReturn(Optional.of(dailyPlan));

        DailyPlanResponse response = dailyPlanQueryService.findOrCreateToday();

        assertThat(response.planDate()).isEqualTo(today);
        then(dailyPlanRepository).should().findByUserIdAndPlanDate(userId, today);
    }

    @Test
    @DisplayName("findOrCreateToday creates a new plan when missing")
    void findOrCreateToday_notFound_createSuccess() {
        givenCurrentUser();
        LocalDate today = LocalDate.now();
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, today)).willReturn(Optional.empty());
        given(dailyPlanRepository.saveAndFlush(any(DailyPlan.class)))
                .willAnswer(invocation -> invocation.getArgument(0, DailyPlan.class));

        DailyPlanResponse response = dailyPlanQueryService.findOrCreateToday();

        assertThat(response.planDate()).isEqualTo(today);
        then(dailyPlanRepository).should().saveAndFlush(any(DailyPlan.class));
    }

    @Test
    @DisplayName("findOrCreateToday reloads the plan when concurrent creation wins")
    void findOrCreateToday_duplicateAtWrite_returnsExistingPlan() {
        givenCurrentUser();
        LocalDate today = LocalDate.now();
        DailyPlan dailyPlan = DailyPlan.create(userId, today);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, today))
                .willReturn(Optional.empty(), Optional.of(dailyPlan));
        given(dailyPlanRepository.saveAndFlush(any(DailyPlan.class)))
                .willThrow(duplicateKeyViolation());

        DailyPlanResponse response = dailyPlanQueryService.findOrCreateToday();

        assertThat(response.planDate()).isEqualTo(today);
        then(dailyPlanRepository).should(times(2)).findByUserIdAndPlanDate(userId, today);
    }

    @Test
    @DisplayName("findOrCreateToday rethrows non-duplicate integrity violations")
    void findOrCreateToday_otherIntegrityViolation_rethrowsException() {
        givenCurrentUser();
        LocalDate today = LocalDate.now();
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, today)).willReturn(Optional.empty());
        given(dailyPlanRepository.saveAndFlush(any(DailyPlan.class)))
                .willThrow(otherIntegrityViolation());

        assertThatThrownBy(() -> dailyPlanQueryService.findOrCreateToday())
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
