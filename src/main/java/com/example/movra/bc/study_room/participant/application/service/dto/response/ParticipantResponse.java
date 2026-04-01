package com.example.movra.bc.study_room.participant.application.service.dto.response;

import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.type.SessionMode;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ParticipantResponse(
        UUID participantId,
        UUID userId,
        SessionMode sessionMode,
        LocalDateTime joinedAt
) {
    public static ParticipantResponse from(Participant participant) {
        return ParticipantResponse.builder()
                .participantId(participant.getId().id())
                .userId(participant.getUserId().id())
                .sessionMode(participant.getSessionMode())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}
