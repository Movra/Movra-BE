package com.example.movra.bc.focus.focus_session.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryItemView;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryView;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.repository.DailyFocusSummaryRepository;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyFocusSummaryReader {

    private final DailyFocusSummaryRepository dailyFocusSummaryRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final Clock clock;

    public List<DailyFocusSummaryView> findRange(UserId userId, LocalDate from, LocalDate to) {
        return dailyFocusSummaryRepository.findWithItemsByUserIdAndDateBetween(userId, from, to).stream()
                .map(this::toView)
                .toList();
    }

    public Optional<DailyFocusSummaryView> findOne(UserId userId, LocalDate date) {
        return dailyFocusSummaryRepository.findWithItemsByUserIdAndDate(userId, date)
                .map(this::toView);
    }

    public List<UserId> findActiveUserIds(LocalDate date) {
        Instant dayStart = date.atStartOfDay(clock.getZone()).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        return focusSessionRepository.findDistinctUserIdsOverlappingPeriod(dayStart, dayEnd);
    }

    private DailyFocusSummaryView toView(DailyFocusSummary summary) {
        List<DailyFocusSummaryItemView> items = summary.getItems().stream()
                .map(item -> new DailyFocusSummaryItemView(
                        item.getStartedAtSnapshot(),
                        item.getEndedAtSnapshot(),
                        item.getRecordedDurationSecondsSnapshot(),
                        item.getOverlapStartedAt(),
                        item.getOverlapEndedAt(),
                        item.getOverlapSeconds(),
                        item.getDisplayOrder()
                ))
                .toList();

        return new DailyFocusSummaryView(
                summary.getUserId(),
                summary.getDate(),
                summary.getTotalSeconds(),
                summary.getSessionCount(),
                items
        );
    }
}
