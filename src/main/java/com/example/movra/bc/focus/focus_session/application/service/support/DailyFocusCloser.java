package com.example.movra.bc.focus.focus_session.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyFocusCloser {

    private final DailyFocusSummaryRepository dailyFocusSummaryRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final DailyFocusSummarySaver dailyFocusSummarySaver;
    private final Clock clock;

    @Transactional(readOnly = true)
    public void close(UserId userId, LocalDate date) {
        if (dailyFocusSummaryRepository.existsByUserIdAndDate(userId, date)) {
            return;
        }

        Instant dayStart = date.atStartOfDay(clock.getZone()).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();

        List<FocusSession> sessions = focusSessionRepository
                .findAllOverlappingPeriod(userId, dayStart, dayEnd);

        if (sessions.isEmpty()) {
            log.debug("No focus sessions for user={}, date={}; skipping DailyFocusSummary close", userId.id(), date);
            return;
        }

        DailyFocusSummary summary = DailyFocusSummary.close(
                sessions, userId, date, dayStart, dayEnd, clock
        );

        boolean saved = dailyFocusSummarySaver.save(summary);
        if (!saved) {
            log.debug("DailyFocusSummary already exists for user={}, date={}", userId.id(), date);
        }
    }
}
