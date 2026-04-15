package com.example.movra.bc.focus.focus_session.application.service.query;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.query.dto.DailyFocusSummaryView;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusDailySummaryQueryService {

    private final DailyFocusSummaryRepository dailyFocusSummaryRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final Clock clock;

    public List<DailyFocusSummaryView> findRange(UserId userId, LocalDate from, LocalDate to) {
        return dailyFocusSummaryRepository.findByUserIdAndDateBetween(userId, from, to).stream()
                .map(s -> new DailyFocusSummaryView(s.getUserId(), s.getDate(), s.getTotalSeconds(), s.getSessionCount()))
                .toList();
    }

    public Optional<DailyFocusSummaryView> findOne(UserId userId, LocalDate date) {
        return dailyFocusSummaryRepository.findByUserIdAndDate(userId, date)
                .map(s -> new DailyFocusSummaryView(s.getUserId(), s.getDate(), s.getTotalSeconds(), s.getSessionCount()));
    }

    public List<UserId> findActiveUserIds(LocalDate date) {
        Instant dayStart = date.atStartOfDay(clock.getZone()).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        return focusSessionRepository.findDistinctUserIdsByStartedAtIn(dayStart, dayEnd);
    }
}
