package com.example.movra.bc.study_room.participant.domain.event;

import java.util.UUID;

public record ParticipantLeftEvent(
        UUID roomId,
        UUID participantId,
        UUID userId
) {
}
