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
import org.springframework.test.util.ReflectionTestUtils;

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

    @Test
    @DisplayName("legacy next_action data remains readable during migration")
    void query_legacyNextAction_returnsFallbackFields() {
        givenCurrentUser();
        DailyReflection dailyReflection = DailyReflection.create(
                userId,
                reflectionDate,
                "Started one important task",
                "Lost focus in the afternoon",
                "temporary if condition",
                "temporary then action"
        );
        ReflectionTestUtils.setField(dailyReflection, "ifCondition", null);
        ReflectionTestUtils.setField(dailyReflection, "thenAction", null);
        ReflectionTestUtils.setField(dailyReflection, "legacyNextAction", "Start the afternoon task in smaller chunks");
        given(dailyReflectionRepository.findByUserIdAndReflectionDate(userId, reflectionDate))
                .willReturn(Optional.of(dailyReflection));

        DailyReflectionResponse response = queryDailyReflectionService.query(reflectionDate);

        assertThat(response.ifCondition()).isEmpty();
        assertThat(response.thenAction()).isEqualTo("Start the afternoon task in smaller chunks");
    }
}
