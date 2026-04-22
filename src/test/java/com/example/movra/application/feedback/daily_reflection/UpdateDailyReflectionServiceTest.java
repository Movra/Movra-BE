package com.example.movra.application.feedback.daily_reflection;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.feedback.daily_reflection.application.exception.DailyReflectionNotFoundException;
import com.example.movra.bc.feedback.daily_reflection.application.service.UpdateDailyReflectionService;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.request.UpdateDailyReflectionRequest;
import com.example.movra.bc.feedback.daily_reflection.domain.DailyReflection;
import com.example.movra.bc.feedback.daily_reflection.domain.exception.InvalidDailyReflectionException;
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
    @DisplayName("update succeeds")
    void update_success() {
        givenCurrentUser();
        DailyReflection dailyReflection = DailyReflection.create(
                userId,
                LocalDate.of(2026, 4, 10),
                "Existing win",
                "Existing breakdown",
                "Existing if condition",
                "Existing then action"
        );
        UpdateDailyReflectionRequest request = new UpdateDailyReflectionRequest(
                "Updated win",
                "Updated breakdown",
                "Updated if condition",
                "Updated then action"
        );
        given(dailyReflectionRepository.findByIdAndUserId(any(DailyReflectionId.class), any(UserId.class)))
                .willReturn(Optional.of(dailyReflection));

        updateDailyReflectionService.update(UUID.randomUUID(), request);

        assertThat(dailyReflection.getWhatWentWell()).isEqualTo("Updated win");
        assertThat(dailyReflection.getWhatBrokeDown()).isEqualTo("Updated breakdown");
        assertThat(dailyReflection.getIfCondition()).isEqualTo("Updated if condition");
        assertThat(dailyReflection.getThenAction()).isEqualTo("Updated then action");
        then(dailyReflectionRepository).should().save(dailyReflection);
    }

    @Test
    @DisplayName("update throws when reflection is missing")
    void update_notFound_throwsException() {
        givenCurrentUser();
        given(dailyReflectionRepository.findByIdAndUserId(any(DailyReflectionId.class), any(UserId.class)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> updateDailyReflectionService.update(
                UUID.randomUUID(),
                new UpdateDailyReflectionRequest("One win", "One breakdown", "If condition", "Then action")
        )).isInstanceOf(DailyReflectionNotFoundException.class);
    }

    @Test
    @DisplayName("update throws when reflection content is invalid")
    void update_invalidContent_throwsException() {
        givenCurrentUser();
        DailyReflection dailyReflection = DailyReflection.create(
                userId,
                LocalDate.of(2026, 4, 10),
                "Existing win",
                "Existing breakdown",
                "Existing if condition",
                "Existing then action"
        );
        given(dailyReflectionRepository.findByIdAndUserId(any(DailyReflectionId.class), any(UserId.class)))
                .willReturn(Optional.of(dailyReflection));

        assertThatThrownBy(() -> updateDailyReflectionService.update(
                UUID.randomUUID(),
                new UpdateDailyReflectionRequest(" ", "Updated breakdown", "Updated if condition", "Updated then action")
        )).isInstanceOf(InvalidDailyReflectionException.class);
    }
}
