package com.example.movra.bc.study_room.participant.domain.vo;


import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ParticipantId(
        UUID id
) {

    public static ParticipantId newId() {
        return new ParticipantId(UUID.randomUUID());
    }

    public static ParticipantId of(UUID participantId) {
        return new ParticipantId(participantId);
    }
}
