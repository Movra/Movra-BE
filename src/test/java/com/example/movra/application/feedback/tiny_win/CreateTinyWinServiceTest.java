package com.example.movra.application.feedback.tiny_win;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.feedback.tiny_win.application.service.CreateTinyWinService;
import com.example.movra.bc.feedback.tiny_win.application.service.dto.request.TinyWinRequest;
import com.example.movra.bc.feedback.tiny_win.domain.TinyWin;
import com.example.movra.bc.feedback.tiny_win.domain.repository.TinyWinRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CreateTinyWinServiceTest {

    @InjectMocks
    private CreateTinyWinService createTinyWinService;

    @Mock
    private TinyWinRepository tinyWinRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    private final UserId userId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("create stores tiny win and records analytics event")
    void create_success() {
        givenCurrentUser();
        given(tinyWinRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0, TinyWin.class));

        createTinyWinService.create(new TinyWinRequest("Win", "Completed one small task"));

        then(tinyWinRepository).should().save(any());
        then(analyticsEventRecorder).should().recordSafely(
                eq(userId),
                eq(AnalyticsEventType.TINY_WIN_CREATED),
                argThat(properties -> properties.containsKey("tinyWinId"))
        );
    }
}
