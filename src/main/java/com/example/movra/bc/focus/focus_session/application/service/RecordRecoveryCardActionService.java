package com.example.movra.bc.focus.focus_session.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.service.dto.request.RecordRecoveryCardActionRequest;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecordRecoveryCardActionService {

    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    public void record(RecordRecoveryCardActionRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        analyticsEventRecorder.recordSafely(
                userId,
                AnalyticsEventType.RECOVERY_CARD_ACTIONED,
                Map.of("action", request.action().name())
        );
    }
}
