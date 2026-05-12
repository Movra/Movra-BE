package com.example.movra.bc.study_room.room.application.service.dto.response;

import com.example.movra.bc.study_room.room.domain.Room;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PublicRoomResponse(
        UUID roomId,
        String name,
        LocalDateTime createdAt
) {

    public static PublicRoomResponse from(Room room) {
        return PublicRoomResponse.builder()
                .roomId(room.getId().id())
                .name(room.getName())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
