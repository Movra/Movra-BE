package com.example.movra.bc.study_room.room.domain;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import com.example.movra.bc.study_room.room.domain.vo.Visibility;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_public_room")
@PrimaryKeyJoinColumn(name = "room_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublicRoom extends Room {

    private PublicRoom(RoomId id, UserId leaderId, String name, LocalDateTime createdAt) {
        super(id, leaderId, name, createdAt);
    }

    public static PublicRoom create(String name, UserId userId) {
        PublicRoom room = new PublicRoom(
                RoomId.newId(),
                userId,
                name,
                LocalDateTime.now()
        );
        room.initialize(userId);
        return room;
    }

}
