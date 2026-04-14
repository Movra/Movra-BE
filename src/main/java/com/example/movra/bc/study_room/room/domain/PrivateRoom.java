package com.example.movra.bc.study_room.room.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.room.domain.exception.InvalidInviteCodeException;
import com.example.movra.bc.study_room.room.domain.exception.PrivateRoomRequiresInviteCodeException;
import com.example.movra.bc.study_room.room.domain.vo.InviteCode;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "tbl_private_room")
@PrimaryKeyJoinColumn(name = "room_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrivateRoom extends Room {

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "invite_code", nullable = false, unique = true))
    private InviteCode inviteCode;

    private PrivateRoom(RoomId id, UserId leaderId, String name, LocalDateTime createdAt, InviteCode inviteCode) {
        super(id, leaderId, name, createdAt);
        this.inviteCode = inviteCode;
    }

    public static PrivateRoom create(String name, UserId userId) {
        PrivateRoom room = new PrivateRoom(
                RoomId.newId(),
                userId,
                name,
                LocalDateTime.now(),
                InviteCode.generate()
        );
        room.initialize(userId);
        return room;
    }

    @Override
    public void join(UserId userId, String inviteCode) {
        if (inviteCode == null) {
            throw new PrivateRoomRequiresInviteCodeException();
        }

        if (!this.inviteCode.code().equals(inviteCode)) {
            throw new InvalidInviteCodeException();
        }

        super.join(userId, inviteCode);
    }
}
