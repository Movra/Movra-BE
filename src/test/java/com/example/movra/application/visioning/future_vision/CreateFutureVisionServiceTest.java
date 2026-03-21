package com.example.movra.application.visioning.future_vision;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.service.CreateFutureVisionService;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionAlreadyExistsException;
import com.example.movra.bc.visioning.future_vision.application.helper.FutureVisionPersister;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.CreateFutureVisionRequest;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CreateFutureVisionServiceTest {

    @InjectMocks
    private CreateFutureVisionService createFutureVisionService;

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
    void create_success() {
        givenCurrentUser();
        given(futureVisionRepository.existsByUserId(userId)).willReturn(false);

        MultipartFile weeklyImage = mock(MultipartFile.class);
        MultipartFile yearlyImage = mock(MultipartFile.class);
        given(imageHelper.upload(weeklyImage, ImageType.FUTURE)).willReturn("weekly-url");
        given(imageHelper.upload(yearlyImage, ImageType.FUTURE)).willReturn("yearly-url");

        createFutureVisionService.create(new CreateFutureVisionRequest(
                weeklyImage,
                yearlyImage,
                "annual vision"
        ));

        then(futureVisionPersister).should().saveFutureVision(
                eq(userId), eq("weekly-url"), eq("yearly-url"), eq("annual vision")
        );
    }

    @Test
    void create_alreadyExists_throwsException() {
        givenCurrentUser();
        given(futureVisionRepository.existsByUserId(userId)).willReturn(true);

        MultipartFile weeklyImage = mock(MultipartFile.class);
        MultipartFile yearlyImage = mock(MultipartFile.class);

        assertThatThrownBy(() -> createFutureVisionService.create(new CreateFutureVisionRequest(
                weeklyImage,
                yearlyImage,
                "annual vision"
        ))).isInstanceOf(FutureVisionAlreadyExistsException.class);
    }
}
