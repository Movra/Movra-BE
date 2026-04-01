package com.example.movra.bc.study_room.room.domain.event;

import com.example.movra.bc.study_room.room.domain.vo.RoomId;

public record RoomDissolvedEvent(
        RoomId roomId
) {
}
