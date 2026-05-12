package com.example.movra.bc.study_room.room.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.room.domain.vo.InviteCode;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_private_room")
@PrimaryKeyJoinColumn(name = "room_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrivateRoom extends Room {

    private PrivateRoom(RoomId id, UserId leaderId, String name, InviteCode inviteCode, LocalDateTime createdAt) {
        super(id, leaderId, name, inviteCode, createdAt);
    }

    public static PrivateRoom create(String name, UserId userId) {
        PrivateRoom room = new PrivateRoom(
                RoomId.newId(),
                userId,
                name,
                InviteCode.generate(),
                LocalDateTime.now()
        );
        room.initialize(userId);
        return room;
    }
}
