package com.example.movra.bc.focus.focus_session.application.service;

import com.example.movra.bc.account.user.application.user.exception.UserNotFoundException;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.exception.FocusSessionNotFoundException;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.FocusSessionResponse;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StopFocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional
    public FocusSessionResponse stop() {
        UserId userId = currentUserQuery.currentUser().userId();
        Instant now = clock.instant();

        userRepository.findByIdForUpdate(userId)
                .orElseThrow(UserNotFoundException::new);

        FocusSession focusSession = focusSessionRepository.findByUserIdAndEndedAtIsNull(userId)
                .orElseThrow(FocusSessionNotFoundException::new);

        focusSession.complete(now);
        analyticsEventRecorder.recordSafely(
                userId,
                analyticsEventType(focusSession),
                analyticsProperties(focusSession, now)
        );
        return FocusSessionResponse.from(focusSession, now);
    }

    private AnalyticsEventType analyticsEventType(FocusSession focusSession) {
        Double completionRate = focusSession.presetCompletionRate();
        if (completionRate != null && completionRate < 1.0) {
            return AnalyticsEventType.FOCUS_SESSION_ABANDONED;
        }

        return AnalyticsEventType.FOCUS_SESSION_COMPLETED;
    }

    private Map<String, String> analyticsProperties(FocusSession focusSession, Instant endedAt) {
        Map<String, String> properties = new HashMap<>();
        properties.put("focusSessionId", focusSession.getId().id().toString());
        properties.put("startedAt", focusSession.getStartedAt().toString());
        properties.put("endedAt", endedAt.toString());
        properties.put("durationSeconds", String.valueOf(focusSession.getDurationSeconds()));

        if (focusSession.getPresetMinutes() != null) {
            properties.put("presetMinutes", String.valueOf(focusSession.getPresetMinutes()));
            properties.put("presetSeconds", String.valueOf(focusSession.presetSeconds()));
        }
        if (focusSession.presetCompletionRate() != null) {
            properties.put("presetCompletionRate", String.valueOf(focusSession.presetCompletionRate()));
        }

        return properties;
    }

}
