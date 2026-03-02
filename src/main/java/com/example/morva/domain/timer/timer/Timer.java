package com.example.morva.domain.timer.timer;

import com.example.morva.domain.account.user.vo.UserId;
import com.example.morva.domain.timer.timer.vo.TimerId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_timers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Timer {

    @EmbeddedId
    private TimerId timerId;

    @Embedded
    private UserId userId;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;
}
