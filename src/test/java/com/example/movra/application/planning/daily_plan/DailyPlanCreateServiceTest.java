package com.example.movra.application.planning.daily_plan;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanAlreadyExistsException;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.DailyPlanCreateService;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.dto.request.DailyPlanRequest;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DailyPlanCreateServiceTest {

    @InjectMocks
    private DailyPlanCreateService dailyPlanCreateService;

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
    @DisplayName("일일 계획 생성 성공")
    void create_success() {
        // given
        givenCurrentUser();
        given(dailyPlanRepository.existsByUserIdAndPlanDate(userId, planDate)).willReturn(false);

        // when
        dailyPlanCreateService.create(new DailyPlanRequest(planDate));

        // then
        then(dailyPlanRepository).should().save(any());
    }

    @Test
    @DisplayName("이미 존재하는 날짜에 일일 계획 생성 시 DailyPlanAlreadyExistsException 발생")
    void create_alreadyExists_throwsException() {
        // given
        givenCurrentUser();
        given(dailyPlanRepository.existsByUserIdAndPlanDate(userId, planDate)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> dailyPlanCreateService.create(new DailyPlanRequest(planDate)))
                .isInstanceOf(DailyPlanAlreadyExistsException.class);
    }
}
