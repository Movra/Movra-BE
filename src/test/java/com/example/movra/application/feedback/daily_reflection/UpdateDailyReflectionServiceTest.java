package com.example.movra.application.feedback.daily_reflection;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.feedback.daily_reflection.application.exception.DailyReflectionNotFoundException;
import com.example.movra.bc.feedback.daily_reflection.application.service.UpdateDailyReflectionService;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.request.UpdateDailyReflectionRequest;
import com.example.movra.bc.feedback.daily_reflection.domain.DailyReflection;
import com.example.movra.bc.feedback.daily_reflection.domain.repository.DailyReflectionRepository;
import com.example.movra.bc.feedback.daily_reflection.domain.vo.DailyReflectionId;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UpdateDailyReflectionServiceTest {

    @InjectMocks
    private UpdateDailyReflectionService updateDailyReflectionService;

    @Mock
    private DailyReflectionRepository dailyReflectionRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("일일 회고 수정 성공")
    void update_success() {
        // given
        givenCurrentUser();
        DailyReflection dailyReflection = DailyReflection.create(
                userId,
                LocalDate.of(2026, 4, 10),
                "기존 잘한 점",
                "기존 실패 지점",
                "기존 다음 행동"
        );
        UpdateDailyReflectionRequest request = new UpdateDailyReflectionRequest(
                "수정된 잘한 점",
                "수정된 실패 지점",
                "수정된 다음 행동"
        );
        given(dailyReflectionRepository.findByIdAndUserId(any(DailyReflectionId.class), any(UserId.class)))
                .willReturn(Optional.of(dailyReflection));

        // when
        updateDailyReflectionService.update(UUID.randomUUID(), request);

        // then
        assertThat(dailyReflection.getWhatWentWell()).isEqualTo("수정된 잘한 점");
        assertThat(dailyReflection.getWhatBrokeDown()).isEqualTo("수정된 실패 지점");
        assertThat(dailyReflection.getNextAction()).isEqualTo("수정된 다음 행동");
        then(dailyReflectionRepository).should().save(dailyReflection);
    }

    @Test
    @DisplayName("회고가 없으면 DailyReflectionNotFoundException 발생")
    void update_notFound_throwsException() {
        // given
        givenCurrentUser();
        given(dailyReflectionRepository.findByIdAndUserId(any(DailyReflectionId.class), any(UserId.class)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updateDailyReflectionService.update(
                UUID.randomUUID(),
                new UpdateDailyReflectionRequest("잘한 점", "실패 지점", "다음 행동")
        )).isInstanceOf(DailyReflectionNotFoundException.class);
    }
}
