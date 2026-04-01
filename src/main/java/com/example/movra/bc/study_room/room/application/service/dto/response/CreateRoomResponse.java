package com.example.movra.bc.study_room.room.application.service.dto.response;

import com.example.movra.bc.study_room.room.domain.PrivateRoom;
import com.example.movra.bc.study_room.room.domain.Room;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateRoomResponse(
        UUID roomId,
        String inviteCode
) {
    public static CreateRoomResponse from(Room room) {
        String code = room instanceof PrivateRoom privateRoom
                ? privateRoom.getInviteCode().code()
                : null;

        return CreateRoomResponse.builder()
                .roomId(room.getId().id())
                .inviteCode(code)
                .build();
    }
}
