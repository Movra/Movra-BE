package com.example.movra.application.feedback.daily_reflection;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.feedback.daily_reflection.application.exception.DailyReflectionNotFoundException;
import com.example.movra.bc.feedback.daily_reflection.application.service.QueryDailyReflectionService;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.response.DailyReflectionResponse;
import com.example.movra.bc.feedback.daily_reflection.domain.DailyReflection;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QueryDailyReflectionServiceTest {

    @InjectMocks
    private QueryDailyReflectionService queryDailyReflectionService;

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
    @DisplayName("날짜로 일일 회고 조회 성공")
    void query_success() {
        // given
        givenCurrentUser();
        DailyReflection dailyReflection = DailyReflection.create(
                userId,
                reflectionDate,
                "시작은 빨랐다",
                "오후에 집중이 흔들렸다",
                "오후에 또 집중이 흔들리면",
                "첫 작업을 10분 단위로 쪼갠다"
        );
        given(dailyReflectionRepository.findByUserIdAndReflectionDate(userId, reflectionDate))
                .willReturn(Optional.of(dailyReflection));

        // when
        DailyReflectionResponse response = queryDailyReflectionService.query(reflectionDate);

        // then
        assertThat(response.reflectionDate()).isEqualTo(reflectionDate);
        assertThat(response.whatWentWell()).isEqualTo("시작은 빨랐다");
    }

    @Test
    @DisplayName("회고가 없으면 DailyReflectionNotFoundException 발생")
    void query_notFound_throwsException() {
        // given
        givenCurrentUser();
        given(dailyReflectionRepository.findByUserIdAndReflectionDate(userId, reflectionDate))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> queryDailyReflectionService.query(reflectionDate))
                .isInstanceOf(DailyReflectionNotFoundException.class);
    }
}
