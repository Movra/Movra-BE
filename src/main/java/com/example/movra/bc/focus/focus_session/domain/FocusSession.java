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

    public static final int DEFAULT_PRESET_MINUTES = 5;

    /** 세션 최대 지속 시간. 이 시간을 넘긴 진행 중 세션은 자동 마감 대상이 되고, 기록 시간도 이 값으로 상한 처리된다. */
    public static final Duration MAX_SESSION_DURATION = Duration.ofHours(8);

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "focus_session_id"))
    private FocusSessionId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    // 진행 중인 세션은 null이며, complete() 시점에 한 번만 설정된다.
    @Column(name = "ended_at")
    private Instant endedAt;

    // (endedAt - startedAt)로 유도 가능한 비정규화 스냅샷.
    // 조회/통계 성능을 위해 보관하며 complete()에서 endedAt과 함께 일관되게 채워진다.
    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "preset_minutes")
    private Integer presetMinutes;

    public static FocusSession start(UserId userId, Instant startedAt) {
        return start(userId, startedAt, DEFAULT_PRESET_MINUTES);
    }

    public static FocusSession start(UserId userId, Instant startedAt, Integer presetMinutes) {
        validateRequired(userId, startedAt);
        validatePresetMinutes(presetMinutes);
        return build(userId, startedAt, presetMinutes);
    }

    /**
     * 프리셋 없이 시작하는 무제한(오픈엔드) 세션. stop 시점까지 진행되며,
     * {@link #MAX_SESSION_DURATION} 상한을 넘기면 자동 마감 대상이 된다.
     */
    public static FocusSession startUnlimited(UserId userId, Instant startedAt) {
        validateRequired(userId, startedAt);
        return build(userId, startedAt, null);
    }

    private static FocusSession build(UserId userId, Instant startedAt, Integer presetMinutes) {
        return FocusSession.builder()
                .id(FocusSessionId.newId())
                .userId(userId)
                .startedAt(startedAt)
                .presetMinutes(presetMinutes)
                .build();
    }

    private static void validateRequired(UserId userId, Instant startedAt) {
        if (userId == null || startedAt == null) {
            throw new InvalidFocusSessionException();
        }
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

        Instant cappedEnd = capToMaxDuration(endedAt);
        this.endedAt = cappedEnd;
        this.durationSeconds = Duration.between(startedAt, cappedEnd).getSeconds();

        registerEvent(new FocusSessionCompletedEvent(
                id.id(),
                userId.id(),
                startedAt,
                this.endedAt,
                this.durationSeconds
        ));
    }

    /**
     * 진행 중인 세션을 {@link #MAX_SESSION_DURATION} 상한 시점으로 자동 마감한다.
     * 스케줄러가 버려진(stop 누락) 세션을 정리할 때 사용한다.
     */
    public void autoClose() {
        complete(autoCloseDeadline());
    }

    public boolean isInProgress() {
        return endedAt == null;
    }

    public boolean isUnlimited() {
        return presetMinutes == null;
    }

    /** 이 시점 이후로는 자동 마감 대상이 된다. */
    public Instant autoCloseDeadline() {
        return startedAt.plus(MAX_SESSION_DURATION);
    }

    public boolean isExpiredAt(Instant now) {
        return isInProgress() && !now.isBefore(autoCloseDeadline());
    }

    public long elapsedSecondsAt(Instant now) {
        long maxSeconds = MAX_SESSION_DURATION.getSeconds();
        if (!isInProgress()) {
            long recorded = durationSeconds != null
                    ? durationSeconds
                    : Duration.between(startedAt, endedAt).getSeconds();
            return Math.min(maxSeconds, recorded);
        }

        long elapsed = Math.max(0L, Duration.between(startedAt, now).getSeconds());
        return Math.min(maxSeconds, elapsed);
    }

    private Instant capToMaxDuration(Instant candidateEnd) {
        Instant maxEnd = autoCloseDeadline();
        return candidateEnd.isAfter(maxEnd) ? maxEnd : candidateEnd;
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
