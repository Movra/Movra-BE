package com.example.movra.bc.study_room.room.application.service.dto.response;

import com.example.movra.bc.study_room.room.domain.Room;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateRoomResponse(
        UUID roomId,
        String inviteCode
) {
    public static CreateRoomResponse from(Room room) {
        return CreateRoomResponse.builder()
                .roomId(room.getId().id())
                .inviteCode(room.getInviteCode().code())
                .build();
    }
}
