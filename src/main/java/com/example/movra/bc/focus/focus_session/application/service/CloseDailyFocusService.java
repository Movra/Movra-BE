package com.example.movra.bc.focus.focus_session.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.focus.focus_session.domain.type.ClosedBy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CloseDailyFocusService {

    private final DailyFocusSummaryRepository dailyFocusSummaryRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final Clock clock;

    @Transactional
    public void close(UserId userId, LocalDate date, ClosedBy closedBy) {
        if (dailyFocusSummaryRepository.existsByUserIdAndDate(userId, date)) {
            return;
        }

        Instant dayStart = date.atStartOfDay(clock.getZone()).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();

        List<FocusSession> sessions = focusSessionRepository
                .findCompletedByUserIdAndStartedAtIn(userId, dayStart, dayEnd);

        long totalSeconds = sessions.stream()
                .mapToLong(FocusSession::getDurationSeconds)
                .sum();
        int sessionCount = sessions.size();

        DailyFocusSummary summary = DailyFocusSummary.close(
                userId, date, totalSeconds, sessionCount, closedBy, clock
        );
        dailyFocusSummaryRepository.save(summary);
    }
}
