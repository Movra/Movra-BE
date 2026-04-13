package com.example.movra.bc.focus.focus_session.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.helper.FocusSessionTimeCalculator;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.FocusSessionResponse;
import com.example.movra.bc.focus.focus_session.application.service.dto.response.TodayFocusSessionsResponse;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryTodayFocusSessionsService {

    private final FocusSessionRepository focusSessionRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;
    private final FocusSessionTimeCalculator focusSessionTimeCalculator;

    @Transactional(readOnly = true)
    public TodayFocusSessionsResponse query() {
        UserId userId = currentUserQuery.currentUser().userId();
        Instant now = clock.instant();
        ZoneId zoneId = clock.getZone();
        LocalDate today = now.atZone(zoneId).toLocalDate();
        Instant startOfDay = today.atStartOfDay(zoneId).toInstant();
        Instant startOfNextDay = today.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<FocusSession> focusSessions = focusSessionRepository
                .findAllOverlappingPeriod(
                        userId,
                        startOfDay,
                        startOfNextDay
                );

        List<FocusSessionResponse> sessions = focusSessions.stream()
                .map(focusSession -> FocusSessionResponse.from(focusSession, now))
                .toList();

        long totalFocusSeconds = focusSessions.stream()
                .mapToLong(focusSession -> focusSessionTimeCalculator.overlapSeconds(
                        focusSession,
                        startOfDay,
                        startOfNextDay,
                        now
                ))
                .sum();

        boolean focusing = focusSessions.stream().anyMatch(FocusSession::isInProgress);

        return TodayFocusSessionsResponse.builder()
                .targetDate(today)
                .queriedAt(now)
                .totalFocusSeconds(totalFocusSeconds)
                .focusing(focusing)
                .sessions(sessions)
                .build();
    }
}
