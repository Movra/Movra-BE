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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

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
    @DisplayName("create succeeds")
    void create_success() {
        givenCurrentUser();
        given(dailyPlanRepository.existsByUserIdAndPlanDate(userId, planDate)).willReturn(false);

        dailyPlanCreateService.create(new DailyPlanRequest(planDate));

        then(dailyPlanRepository).should().saveAndFlush(any());
    }

    @Test
    @DisplayName("create throws when daily plan already exists")
    void create_alreadyExists_throwsException() {
        givenCurrentUser();
        given(dailyPlanRepository.existsByUserIdAndPlanDate(userId, planDate)).willReturn(true);

        assertThatThrownBy(() -> dailyPlanCreateService.create(new DailyPlanRequest(planDate)))
                .isInstanceOf(DailyPlanAlreadyExistsException.class);
    }

    @Test
    @DisplayName("create converts unique constraint violations to DailyPlanAlreadyExistsException")
    void create_duplicateAtWrite_throwsException() {
        givenCurrentUser();
        given(dailyPlanRepository.existsByUserIdAndPlanDate(userId, planDate)).willReturn(false);
        given(dailyPlanRepository.saveAndFlush(any()))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> dailyPlanCreateService.create(new DailyPlanRequest(planDate)))
                .isInstanceOf(DailyPlanAlreadyExistsException.class);
    }
}
