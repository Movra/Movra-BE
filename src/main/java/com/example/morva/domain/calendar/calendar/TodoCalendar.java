package com.example.morva.domain.calendar.calendar;

import com.example.morva.domain.account.user.vo.UserId;
import com.example.morva.domain.calendar.calendar.vo.TodoCalendarId;
import com.example.morva.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_calendar")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TodoCalendar extends AbstractAggregateRoot {

    @EmbeddedId
    private TodoCalendarId todoCalendarId;

    @Embedded
    private UserId userId;

    @Column(nullable = false)
    private LocalDateTime calendarDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isCompleted;
}
