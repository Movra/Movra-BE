package com.example.movra.application.visioning.future_vision;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.application.CreateFutureVisionService;
import com.example.movra.bc.visioning.future_vision.application.exception.FutureVisionAlreadyExistsException;
import com.example.movra.bc.visioning.future_vision.application.service.dto.request.CreateFutureVisionRequest;
import com.example.movra.bc.visioning.future_vision.domain.repository.FutureVisionRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CreateFutureVisionServiceTest {

    @InjectMocks
    private CreateFutureVisionService createFutureVisionService;

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
    void create_success() {
        givenCurrentUser();
        given(futureVisionRepository.existsByUserId(userId)).willReturn(false);

        createFutureVisionService.create(new CreateFutureVisionRequest(
                "weekly.png",
                "yearly.png",
                "annual vision"
        ));

        then(futureVisionRepository).should().save(any());
    }

    @Test
    void create_alreadyExists_throwsException() {
        givenCurrentUser();
        given(futureVisionRepository.existsByUserId(userId)).willReturn(true);

        assertThatThrownBy(() -> createFutureVisionService.create(new CreateFutureVisionRequest(
                "weekly.png",
                "yearly.png",
                "annual vision"
        ))).isInstanceOf(FutureVisionAlreadyExistsException.class);
    }
}
