package com.example.movra.application.visioning.future_vision;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.service.UpdateWeeklyVisionService;
import com.example.movra.bc.visioning.future_vision.application.service.UpdateYearlyVisionService;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionNotFoundException;
import com.example.movra.bc.visioning.future_vision.application.helper.FutureVisionPersister;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.UpdateWeeklyVisionRequest;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.UpdateYearlyVisionRequest;
import com.example.movra.bc.visioning.future_vision.domain.FutureVision;
import com.example.movra.bc.visioning.future_vision.domain.repository.FutureVisionRepository;
import com.example.movra.sharedkernel.file.storage.ImageHelper;
import com.example.movra.sharedkernel.file.storage.type.ImageType;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UpdateFutureVisionServiceTest {

    @InjectMocks
    private UpdateWeeklyVisionService updateWeeklyVisionService;

    @InjectMocks
    private UpdateYearlyVisionService updateYearlyVisionService;

    @Mock
    private FutureVisionRepository futureVisionRepository;

    @Mock
    private FutureVisionPersister futureVisionPersister;

    @Mock
    private ImageHelper imageHelper;

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

        MultipartFile newWeeklyImage = mock(MultipartFile.class);
        given(imageHelper.update("weekly.png", newWeeklyImage, ImageType.FUTURE)).willReturn("weekly-updated.png");

        updateWeeklyVisionService.update(new UpdateWeeklyVisionRequest(newWeeklyImage));

        then(futureVisionPersister).should().saveFutureVision(futureVision);
    }

    @Test
    void updateYearly_success() {
        givenCurrentUser();
        FutureVision futureVision = FutureVision.create(userId, "weekly.png", "yearly.png", "annual vision");
        given(futureVisionRepository.findByUserId(userId)).willReturn(Optional.of(futureVision));

        MultipartFile newYearlyImage = mock(MultipartFile.class);
        given(imageHelper.update("yearly.png", newYearlyImage, ImageType.FUTURE)).willReturn("yearly-updated.png");

        updateYearlyVisionService.update(new UpdateYearlyVisionRequest(newYearlyImage, "updated vision"));

        then(futureVisionPersister).should().saveFutureVision(futureVision);
    }

    @Test
    void update_notFound_throwsException() {
        givenCurrentUser();
        given(futureVisionRepository.findByUserId(userId)).willReturn(Optional.empty());

        MultipartFile newWeeklyImage = mock(MultipartFile.class);

        assertThatThrownBy(() -> updateWeeklyVisionService.update(new UpdateWeeklyVisionRequest(newWeeklyImage)))
                .isInstanceOf(FutureVisionNotFoundException.class);
    }
}
