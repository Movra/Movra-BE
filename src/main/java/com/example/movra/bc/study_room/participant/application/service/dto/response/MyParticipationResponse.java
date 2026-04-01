package com.example.movra.bc.study_room.participant.application.service.dto.response;

import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.type.SessionMode;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MyParticipationResponse(
        UUID roomId,
        UUID participantId,
        SessionMode sessionMode,
        LocalDateTime joinedAt
) {
    public static MyParticipationResponse from(Participant participant) {
        return MyParticipationResponse.builder()
                .roomId(participant.getRoomId().id())
                .participantId(participant.getId().id())
                .sessionMode(participant.getSessionMode())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}
