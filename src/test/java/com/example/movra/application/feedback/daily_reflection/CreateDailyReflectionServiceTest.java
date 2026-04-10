package com.example.movra.application.feedback.daily_reflection;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.feedback.daily_reflection.application.exception.DailyReflectionAlreadyExistsException;
import com.example.movra.bc.feedback.daily_reflection.application.service.CreateDailyReflectionService;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.request.CreateDailyReflectionRequest;
import com.example.movra.bc.feedback.daily_reflection.domain.repository.DailyReflectionRepository;
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
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CreateDailyReflectionServiceTest {

    @InjectMocks
    private CreateDailyReflectionService createDailyReflectionService;

    @Mock
    private DailyReflectionRepository dailyReflectionRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();
    private final LocalDate reflectionDate = LocalDate.of(2026, 4, 10);

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("일일 회고 생성 성공")
    void create_success() {
        // given
        givenCurrentUser();
        CreateDailyReflectionRequest request = new CreateDailyReflectionRequest(
                reflectionDate,
                "핵심 작업 하나는 시작했다",
                "오후 집중이 무너졌다",
                "내일은 오후 작업을 더 가볍게 시작한다"
        );
        given(dailyReflectionRepository.existsByUserIdAndReflectionDate(userId, reflectionDate)).willReturn(false);

        // when
        createDailyReflectionService.create(request);

        // then
        then(dailyReflectionRepository).should().save(any());
    }

    @Test
    @DisplayName("같은 날짜 회고가 이미 있으면 DailyReflectionAlreadyExistsException 발생")
    void create_alreadyExists_throwsException() {
        // given
        givenCurrentUser();
        CreateDailyReflectionRequest request = new CreateDailyReflectionRequest(
                reflectionDate,
                "잘한 점",
                "실패한 점",
                "다음 행동"
        );
        given(dailyReflectionRepository.existsByUserIdAndReflectionDate(userId, reflectionDate)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> createDailyReflectionService.create(request))
                .isInstanceOf(DailyReflectionAlreadyExistsException.class);
    }
}
