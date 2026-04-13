package com.example.movra.bc.focus.focus_session.application.service.dto.response;

import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FocusSessionResponse(
        UUID focusSessionId,
        Instant startedAt,
        Instant endedAt,
        Long recordedElapsedSeconds,
        long elapsedSeconds,
        boolean inProgress
) {

    public static FocusSessionResponse from(FocusSession focusSession, Instant now) {
        return FocusSessionResponse.builder()
                .focusSessionId(focusSession.getId().id())
                .startedAt(focusSession.getStartedAt())
                .endedAt(focusSession.getEndedAt())
                .recordedElapsedSeconds(focusSession.getDurationSeconds())
                .elapsedSeconds(focusSession.elapsedSecondsAt(now))
                .inProgress(focusSession.isInProgress())
                .build();
    }
}
