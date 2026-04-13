package com.example.movra.bc.focus.focus_session.domain.event;

import java.time.Instant;
import java.util.UUID;

public record FocusSessionCompletedEvent(
        UUID focusSessionId,
        UUID userId,
        Instant startedAt,
        Instant endedAt,
        long durationSeconds
) {
}
