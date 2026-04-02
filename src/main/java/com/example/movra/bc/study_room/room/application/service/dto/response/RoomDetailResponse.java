package com.example.movra.bc.study_room.room.application.service.dto.response;

import com.example.movra.bc.study_room.participant.application.service.dto.response.ParticipantResponse;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.room.domain.Room;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record RoomDetailResponse(
        UUID roomId,
        String name,
        UUID leaderUserId,
        int currentCount,
        LocalDateTime createdAt,
        List<ParticipantResponse> participants
) {
    public static RoomDetailResponse from(Room room, List<Participant> participants) {
        List<ParticipantResponse> participantResponses = participants.stream()
                .map(ParticipantResponse::from)
                .toList();

        return RoomDetailResponse.builder()
                .roomId(room.getId().id())
                .name(room.getName())
                .leaderUserId(room.getLeaderId().id())
                .currentCount(participantResponses.size())
                .createdAt(room.getCreatedAt())
                .participants(participantResponses)
                .build();
    }
}
