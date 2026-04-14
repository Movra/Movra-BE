package com.example.movra.application.planning.daily_plan.top_pick;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.QueryTopPicksService;
import com.example.movra.bc.planning.daily_plan.application.service.task.top_pick.dto.response.TopPicksResponse;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QueryTopPicksServiceTest {

    @InjectMocks
    private QueryTopPicksService queryTopPicksService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    @BeforeEach
    void setUp() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build());
    }

    @Test
    @DisplayName("Top Pick 목록 조회 성공")
    void queryAll_success() {
        // given
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
        dailyPlan.addTask("Top Pick 할 일");
        dailyPlan.addTask("일반 할 일");
        dailyPlan.markAsTopPicked(dailyPlan.getTasks().get(0).getTaskId(), 30, "중요");
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId)).willReturn(Optional.of(dailyPlan));

        // when
        List<TopPicksResponse> responses = queryTopPicksService.queryAll(dailyPlanId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).content()).isEqualTo("Top Pick 할 일");
        assertThat(responses.get(0).estimatedMinutes()).isEqualTo(30);
        assertThat(responses.get(0).memo()).isEqualTo("중요");
    }

    @Test
    @DisplayName("Top Pick이 없으면 빈 목록 반환")
    void queryAll_noTopPicks_returnsEmpty() {
        // given
        DailyPlan dailyPlan = DailyPlan.create(userId, LocalDate.of(2026, 3, 17));
        dailyPlan.addTask("일반 할 일");
        UUID dailyPlanId = dailyPlan.getDailyPlanId().id();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId)).willReturn(Optional.of(dailyPlan));

        // when
        List<TopPicksResponse> responses = queryTopPicksService.queryAll(dailyPlanId);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 DailyPlan 조회 시 DailyPlanNotFoundException 발생")
    void queryAll_dailyPlanNotFound_throwsException() {
        // given
        UUID dailyPlanId = UUID.randomUUID();
        given(dailyPlanRepository.findByDailyPlanIdAndUserId(DailyPlanId.of(dailyPlanId), userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> queryTopPicksService.queryAll(dailyPlanId))
                .isInstanceOf(DailyPlanNotFoundException.class);
    }
}
