package com.example.movra.bc.study_room.participant.domain.event;

import java.time.Duration;
import java.util.UUID;

public record FocusTimeRecordedEvent(
        UUID roomId,
        UUID participantId,
        UUID userId,
        Long totalFocusTime
) {
}
