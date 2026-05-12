package com.example.movra.application.focus.focus_session;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.service.RecordRecoveryCardActionService;
import com.example.movra.bc.focus.focus_session.application.service.dto.request.RecordRecoveryCardActionRequest;
import com.example.movra.bc.focus.focus_session.domain.type.RecoveryCardAction;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RecordRecoveryCardActionServiceTest {

    @InjectMocks
    private RecordRecoveryCardActionService recordRecoveryCardActionService;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private AnalyticsEventRecorder analyticsEventRecorder;

    private final UserId userId = UserId.newId();

    @BeforeEach
    void setUp() {
        given(currentUserQuery.currentUser()).willReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    void record_start_recordsActionedEvent() {
        recordRecoveryCardActionService.record(new RecordRecoveryCardActionRequest(RecoveryCardAction.START));

        then(analyticsEventRecorder).should()
                .recordSafely(eq(userId), eq(AnalyticsEventType.RECOVERY_CARD_ACTIONED), eq(Map.of("action", "START")));
    }

    @Test
    void record_dismiss_recordsActionedEvent() {
        recordRecoveryCardActionService.record(new RecordRecoveryCardActionRequest(RecoveryCardAction.DISMISS));

        then(analyticsEventRecorder).should()
                .recordSafely(eq(userId), eq(AnalyticsEventType.RECOVERY_CARD_ACTIONED), eq(Map.of("action", "DISMISS")));
    }
}
