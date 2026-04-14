package com.example.movra.bc.focus.focus_session.application.service;

import com.example.movra.bc.account.user.application.user.exception.UserNotFoundException;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.exception.FocusSessionAlreadyInProgressException;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.FocusSessionResponse;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StartFocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional
    public FocusSessionResponse start() {
        UserId userId = currentUserQuery.currentUser().userId();
        Instant now = clock.instant();

        userRepository.findByIdForUpdate(userId)
                .orElseThrow(UserNotFoundException::new);

        if (focusSessionRepository.existsByUserIdAndEndedAtIsNull(userId)) {
            throw new FocusSessionAlreadyInProgressException();
        }

        FocusSession focusSession = focusSessionRepository.save(FocusSession.start(userId, now));
        return FocusSessionResponse.from(focusSession, now);
    }
}
