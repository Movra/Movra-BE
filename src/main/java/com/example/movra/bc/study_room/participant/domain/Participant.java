package com.example.movra.bc.study_room.participant.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.domain.event.FocusTimeRecordedEvent;
import com.example.movra.bc.study_room.participant.domain.event.ParticipantLeftEvent;
import com.example.movra.bc.study_room.participant.domain.exception.AlreadyFocusingException;
import com.example.movra.bc.study_room.participant.domain.exception.NotFocusingException;
import com.example.movra.bc.study_room.participant.domain.type.SessionMode;
import com.example.movra.bc.study_room.participant.domain.vo.ParticipantId;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_participant", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "room_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Participant extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "participant_id"))
    private ParticipantId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "room_id", nullable = false))
    private RoomId roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionMode sessionMode;

    @Embedded
    private FocusTimer focusTimer;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    public static Participant enter(UserId userId, RoomId roomId) {
        if (userId == null || roomId == null) {
            throw new IllegalArgumentException("userId and roomId must not be null");
        }

        return Participant.builder()
                .id(ParticipantId.newId())
                .userId(userId)
                .roomId(roomId)
                .sessionMode(SessionMode.REST)
                .focusTimer(FocusTimer.init())
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public void startFocus() {
        if (isFocusing()) {
            throw new AlreadyFocusingException();
        }

        this.sessionMode = SessionMode.FOCUS;
        this.focusTimer = focusTimer.start();
    }

    public void takeBreak() {
        if (!isFocusing()) {
            throw new NotFocusingException();
        }

        this.sessionMode = SessionMode.REST;
        this.focusTimer = focusTimer.pause();
    }

    public Duration leaveAndRecordTime() {
        Duration totalFocusTime = isFocusing()
                ? focusTimer.totalElapsedUntilNow()
                : focusTimer.totalElapsed();

        registerEvent(new ParticipantLeftEvent(roomId.id(), id.id(), userId.id()));
        registerEvent(new FocusTimeRecordedEvent(
                roomId.id(),
                id.id(),
                userId.id(),
                totalFocusTime.getSeconds()
        ));

        return totalFocusTime;
    }

    private boolean isFocusing() {
        return sessionMode == SessionMode.FOCUS;
    }
}
