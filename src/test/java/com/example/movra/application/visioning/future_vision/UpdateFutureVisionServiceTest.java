package com.example.movra.application.visioning.future_vision;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.UpdateWeeklyVisionService;
import com.example.movra.bc.visioning.future_vision.application.UpdateYearlyVisionService;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionNotFoundException;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.UpdateWeeklyVisionRequest;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.UpdateYearlyVisionRequest;
import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import com.example.movra.bc.visioning.future_vision.domain.repository.FutureVisionRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UpdateFutureVisionServiceTest {

    @InjectMocks
    private UpdateWeeklyVisionService updateWeeklyVisionService;

    @InjectMocks
    private UpdateYearlyVisionService updateYearlyVisionService;

    @Mock
    private FutureVisionRepository futureVisionRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    void updateWeekly_success() {
        givenCurrentUser();
        FutureVision futureVision = FutureVision.create(userId, "weekly.png", "yearly.png", "annual vision");
        given(futureVisionRepository.findByUserId(userId)).willReturn(Optional.of(futureVision));

        updateWeeklyVisionService.update(new UpdateWeeklyVisionRequest("weekly-updated.png"));

        assertThat(futureVision.getWeeklyVisionImageUrl()).isEqualTo("weekly-updated.png");
        then(futureVisionRepository).should().save(futureVision);
    }

    @Test
    void updateYearly_success() {
        givenCurrentUser();
        FutureVision futureVision = FutureVision.create(userId, "weekly.png", "yearly.png", "annual vision");
        given(futureVisionRepository.findByUserId(userId)).willReturn(Optional.of(futureVision));

        updateYearlyVisionService.update(new UpdateYearlyVisionRequest("yearly-updated.png", "updated vision"));

        assertThat(futureVision.getYearlyVisionImageUrl()).isEqualTo("yearly-updated.png");
        assertThat(futureVision.getYearlyVisionDescription()).isEqualTo("updated vision");
        then(futureVisionRepository).should().save(futureVision);
    }

    @Test
    void update_notFound_throwsException() {
        givenCurrentUser();
        given(futureVisionRepository.findByUserId(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> updateWeeklyVisionService.update(new UpdateWeeklyVisionRequest("weekly-updated.png")))
                .isInstanceOf(FutureVisionNotFoundException.class);
    }
}
