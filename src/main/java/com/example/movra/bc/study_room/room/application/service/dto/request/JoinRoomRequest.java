package com.example.movra.bc.study_room.room.application.service.dto.request;

import java.util.UUID;

public record JoinRoomRequest(
        UUID roomId,
        String inviteCode
) {
}
