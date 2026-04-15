package com.example.movra.bc.focus.focus_session.domain.event;

import com.example.movra.bc.account.user.domain.user.vo.UserId;

import java.time.LocalDate;

public record DailyFocusSummarizedEvent(
        UserId userId,
        LocalDate date,
        long totalSeconds,
        int sessionCount
) {
}
