package com.example.movra.bc.focus.focus_session.application.service;

import com.example.movra.bc.account.user.application.user.exception.UserNotFoundException;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.focus.focus_session.application.exception.FocusSessionAlreadyInProgressException;
import com.example.movra.bc.focus.focus_session.application.service.dto.request.StartFocusSessionRequest;
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
public class StartFocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional
    public FocusSessionResponse start(StartFocusSessionRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        Instant now = clock.instant();
        Integer presetMinutes = request == null ? null : request.presetMinutes();

        userRepository.findByIdForUpdate(userId)
                .orElseThrow(UserNotFoundException::new);

        if (focusSessionRepository.existsByUserIdAndEndedAtIsNull(userId)) {
            throw new FocusSessionAlreadyInProgressException();
        }

        FocusSession focusSession = focusSessionRepository.save(createSession(userId, now, presetMinutes));
        analyticsEventRecorder.recordSafely(
                userId,
                AnalyticsEventType.FOCUS_SESSION_STARTED,
                startedProperties(focusSession)
        );
        return FocusSessionResponse.from(focusSession, now);
    }

    private FocusSession createSession(UserId userId, Instant now, Integer presetMinutes) {
        return presetMinutes == null
                ? FocusSession.startUnlimited(userId, now)
                : FocusSession.start(userId, now, presetMinutes);
    }

    private Map<String, String> startedProperties(FocusSession focusSession) {
        Map<String, String> properties = new HashMap<>();
        properties.put("focusSessionId", focusSession.getId().id().toString());
        properties.put("startedAt", focusSession.getStartedAt().toString());
        properties.put("unlimited", String.valueOf(focusSession.isUnlimited()));

        if (focusSession.getPresetMinutes() != null) {
            properties.put("presetMinutes", String.valueOf(focusSession.getPresetMinutes()));
            properties.put("presetSeconds", String.valueOf(focusSession.presetSeconds()));
        }

        return properties;
    }
}
