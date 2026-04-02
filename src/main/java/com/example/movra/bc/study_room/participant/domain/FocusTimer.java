package com.example.movra.bc.study_room.participant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class FocusTimer {

    @Column(nullable = false)
    private long elapsedSeconds;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    static FocusTimer init() {
        return new FocusTimer(0L, LocalDateTime.now());
    }

    FocusTimer start() {
        return new FocusTimer(this.elapsedSeconds, LocalDateTime.now());
    }

    FocusTimer pause() {
        long additional = Duration.between(startedAt, LocalDateTime.now()).getSeconds();
        return new FocusTimer(this.elapsedSeconds + additional, LocalDateTime.now());
    }

    Duration totalElapsed() {
        return Duration.ofSeconds(elapsedSeconds);
    }

    Duration totalElapsedUntilNow() {
        long additional = Duration.between(startedAt, LocalDateTime.now()).getSeconds();
        return Duration.ofSeconds(elapsedSeconds + additional);
    }
}
