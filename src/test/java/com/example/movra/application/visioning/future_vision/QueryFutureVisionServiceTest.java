package com.example.movra.application.visioning.future_vision;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.service.QueryFutureVisionService;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionNotFoundException;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.FutureVisionResponse;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.WeeklyVisionResponse;
import com.example.movra.bc.visioning.future_vision.application.service.dto.response.YearlyVisionResponse;
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
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QueryFutureVisionServiceTest {

    @InjectMocks
    private QueryFutureVisionService queryFutureVisionService;

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
    void query_success() {
        givenCurrentUser();
        FutureVision futureVision = FutureVision.create(userId, "weekly.png", "yearly.png", "annual vision");
        given(futureVisionRepository.findByUserId(userId)).willReturn(Optional.of(futureVision));

        FutureVisionResponse response = queryFutureVisionService.query();

        assertThat(response.weeklyVisionImageUrl()).isEqualTo("weekly.png");
        assertThat(response.yearlyVisionImageUrl()).isEqualTo("yearly.png");
        assertThat(response.yearlyVisionDescription()).isEqualTo("annual vision");
    }

    @Test
    void queryWeeklyVision_success() {
        givenCurrentUser();
        FutureVision futureVision = FutureVision.create(userId, "weekly.png", "yearly.png", "annual vision");
        given(futureVisionRepository.findByUserId(userId)).willReturn(Optional.of(futureVision));

        WeeklyVisionResponse response = queryFutureVisionService.queryWeeklyVision();

        assertThat(response.weeklyVisionImageUrl()).isEqualTo("weekly.png");
    }

    @Test
    void queryYearlyVision_success() {
        givenCurrentUser();
        FutureVision futureVision = FutureVision.create(userId, "weekly.png", "yearly.png", "annual vision");
        given(futureVisionRepository.findByUserId(userId)).willReturn(Optional.of(futureVision));

        YearlyVisionResponse response = queryFutureVisionService.queryYearlyVision();

        assertThat(response.yearlyVisionImageUrl()).isEqualTo("yearly.png");
        assertThat(response.yearlyVisionDescription()).isEqualTo("annual vision");
    }

    @Test
    void query_notFound_throwsException() {
        givenCurrentUser();
        given(futureVisionRepository.findByUserId(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> queryFutureVisionService.query())
                .isInstanceOf(FutureVisionNotFoundException.class);
    }
}
