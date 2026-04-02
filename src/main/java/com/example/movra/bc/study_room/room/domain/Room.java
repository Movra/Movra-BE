package com.example.movra.bc.study_room.room.domain;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.study_room.room.domain.event.ParticipantJoinedEvent;
import com.example.movra.bc.study_room.room.domain.event.ParticipantKickedEvent;
import com.example.movra.bc.study_room.room.domain.event.RoomCreatedEvent;
import com.example.movra.bc.study_room.room.domain.event.RoomDissolvedEvent;
import com.example.movra.bc.study_room.room.domain.exception.LeaderCannotKickSelfException;
import com.example.movra.bc.study_room.room.domain.exception.NotLeaderException;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import com.example.movra.bc.study_room.room.domain.vo.Visibility;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "tbl_room")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Room extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "room_id", unique = true))
    private RoomId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "leader_id", nullable = false))
    private UserId leaderId;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Room(RoomId id, UserId leaderId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.leaderId = leaderId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static Room create(String name, UserId userId, Visibility visibility) {
        return switch (visibility) {
            case PUBLIC -> PublicRoom.create(name, userId);
            case PRIVATE -> PrivateRoom.create(name, userId);
        };
    }

    protected void initialize(UserId userId) {
        registerEvent(new RoomCreatedEvent(this.id, userId));
    }

    public void join(UserId userId, String inviteCode) {
        registerEvent(new ParticipantJoinedEvent(this.id, userId));
    }

    public void kick(UserId leaderId, UserId targetId) {
        validateLeader(leaderId);

        if (leaderId.equals(targetId)) {
            throw new LeaderCannotKickSelfException();
        }

        registerEvent(new ParticipantKickedEvent(this.id, targetId));
    }

    public void dissolve() {
        registerEvent(new RoomDissolvedEvent(this.id));
    }

    public void reassignLeader(UserId newLeaderId) {
        if (newLeaderId == null) {
            throw new IllegalArgumentException("newLeaderId must not be null");
        }

        this.leaderId = newLeaderId;
    }

    private void validateLeader(UserId userId) {
        if (!this.leaderId.equals(userId)) {
            throw new NotLeaderException();
        }
    }
}
