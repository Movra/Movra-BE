package com.example.movra.bc.study_room.room.domain.event;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;

public record RoomCreatedEvent(
        RoomId roomId,
        UserId userId
) {
}
