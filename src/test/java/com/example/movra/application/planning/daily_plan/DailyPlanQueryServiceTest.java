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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

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

    @Test
    @DisplayName("날짜별 일일 계획 조회 성공")
    void findByPlanDate_success() {
        // given
        givenCurrentUser();
        DailyPlan dailyPlan = DailyPlan.create(userId, planDate);
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, planDate)).willReturn(Optional.of(dailyPlan));

        // when
        DailyPlanResponse response = dailyPlanQueryService.findByPlanDate(planDate);

        // then
        assertThat(response.planDate()).isEqualTo(planDate);
        assertThat(response.tasks()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 날짜 조회 시 DailyPlanNotFoundException 발생")
    void findByPlanDate_notFound_throwsException() {
        // given
        givenCurrentUser();
        given(dailyPlanRepository.findByUserIdAndPlanDate(userId, planDate)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dailyPlanQueryService.findByPlanDate(planDate))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }
}
