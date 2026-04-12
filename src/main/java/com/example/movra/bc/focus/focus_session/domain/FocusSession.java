package com.example.movra.bc.focus.focus_session.domain;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.event.FocusSessionCompletedEvent;
import com.example.movra.bc.focus.focus_session.domain.exception.FocusSessionAlreadyCompletedException;
import com.example.movra.bc.focus.focus_session.domain.vo.FocusSessionId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_focus_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FocusSession extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "focus_session_id"))
    private FocusSessionId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    public static FocusSession start(UserId userId, Instant startedAt) {
        return FocusSession.builder()
                .id(FocusSessionId.newId())
                .userId(userId)
                .startedAt(startedAt)
                .build();
    }

    public void complete(Instant endedAt) {
        if (!isInProgress()) {
            throw new FocusSessionAlreadyCompletedException();
        }

        if (endedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("endedAt must not be before startedAt");
        }

        this.endedAt = endedAt;
        this.durationSeconds = Duration.between(startedAt, endedAt).getSeconds();

        registerEvent(new FocusSessionCompletedEvent(
                id.id(),
                userId.id(),
                startedAt,
                endedAt,
                durationSeconds
        ));
    }

    public boolean isInProgress() {
        return endedAt == null;
    }

    public long elapsedSecondsAt(Instant now) {
        if (!isInProgress()) {
            return durationSeconds == null ? 0L : durationSeconds;
        }

        return Math.max(0L, Duration.between(startedAt, now).getSeconds());
    }

    public long elapsedSecondsWithin(Instant periodStart, Instant periodEnd, Instant now) {
        Instant effectiveEnd = isInProgress() ? now : endedAt;
        Instant overlapStart = startedAt.isAfter(periodStart) ? startedAt : periodStart;
        Instant overlapEnd = effectiveEnd.isBefore(periodEnd) ? effectiveEnd : periodEnd;

        if (!overlapEnd.isAfter(overlapStart)) {
            return 0L;
        }

        return Duration.between(overlapStart, overlapEnd).getSeconds();
    }
}
