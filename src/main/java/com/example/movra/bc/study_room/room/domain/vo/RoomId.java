package com.example.movra.bc.study_room.room.domain.vo;


import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record RoomId(
        UUID id
) {

    public static RoomId newId() {
        return new RoomId(UUID.randomUUID());
    }

    public static RoomId of(UUID roomId) {
        return new RoomId(roomId);
    }
}
