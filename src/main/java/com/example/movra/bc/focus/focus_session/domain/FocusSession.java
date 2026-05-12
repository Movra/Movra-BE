package com.example.movra.bc.focus.focus_session.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.event.FocusSessionCompletedEvent;
import com.example.movra.bc.focus.focus_session.domain.exception.FocusSessionAlreadyCompletedException;
import com.example.movra.bc.focus.focus_session.domain.exception.InvalidFocusSessionException;
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
import java.util.Set;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_focus_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FocusSession extends AbstractAggregateRoot {

    public static final Set<Integer> ALLOWED_PRESET_MINUTES = Set.of(3, 5, 10, 25);

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "focus_session_id"))
    private FocusSessionId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt; //TODO -> NULL 가능

    @Column(name = "duration_seconds")
    private Long durationSeconds; //TODO -> 함수 종속성 문제

    @Column(name = "preset_minutes")
    private Integer presetMinutes;

    public static FocusSession start(UserId userId, Instant startedAt) {
        return start(userId, startedAt, 5);
    }

    public static FocusSession start(UserId userId, Instant startedAt, Integer presetMinutes) {
        if (userId == null) {
            throw new InvalidFocusSessionException();
        }

        if (startedAt == null) {
            throw new InvalidFocusSessionException();
        }

        validatePresetMinutes(presetMinutes);

        return FocusSession.builder()
                .id(FocusSessionId.newId())
                .userId(userId)
                .startedAt(startedAt)
                .presetMinutes(presetMinutes)
                .build();
    }

    public void complete(Instant endedAt) {
        if (!isInProgress()) {
            throw new FocusSessionAlreadyCompletedException();
        }

        if (endedAt == null) {
            throw new InvalidFocusSessionException();
        }

        if (endedAt.isBefore(startedAt)) {
            throw new InvalidFocusSessionException();
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
            return durationSeconds;
        }

        return Math.max(0L, Duration.between(startedAt, now).getSeconds());
    }

    public Integer presetSeconds() {
        if (presetMinutes == null) {
            return null;
        }
        return presetMinutes * 60;
    }

    public Double presetCompletionRate() {
        Integer presetSeconds = presetSeconds();
        if (durationSeconds == null || presetSeconds == null || presetSeconds == 0) {
            return null;
        }
        return (double) durationSeconds / presetSeconds;
    }

    private static void validatePresetMinutes(Integer presetMinutes) {
        if (presetMinutes == null || !ALLOWED_PRESET_MINUTES.contains(presetMinutes)) {
            throw new InvalidFocusSessionException();
        }
    }
}
