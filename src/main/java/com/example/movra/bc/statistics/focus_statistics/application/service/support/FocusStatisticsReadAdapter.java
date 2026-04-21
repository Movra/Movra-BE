package com.example.movra.bc.statistics.focus_statistics.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.application.service.support.DailyFocusSummaryReader;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryItemView;
import com.example.movra.bc.focus.focus_session.application.service.support.dto.DailyFocusSummaryView;
import com.example.movra.bc.focus.focus_session.domain.FocusSession;
import com.example.movra.bc.focus.focus_session.domain.repository.FocusSessionRepository;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsPeriod;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSessionView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryItemView;
import com.example.movra.bc.statistics.focus_statistics.application.service.support.dto.FocusStatisticsSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusStatisticsReadAdapter implements FocusStatisticsReadPort {

    private final DailyFocusSummaryReader dailyFocusSummaryReader;
    private final FocusSessionRepository focusSessionRepository;

    @Override
    public Optional<FocusStatisticsSummaryView> findSummary(UserId userId, LocalDate date) {
        return dailyFocusSummaryReader.findOne(userId, date)
                .map(this::toSummaryView);
    }

    @Override
    public List<FocusStatisticsSummaryView> findSummaryRange(UserId userId, LocalDate from, LocalDate to) {
        return dailyFocusSummaryReader.findRange(userId, from, to).stream()
                .map(this::toSummaryView)
                .toList();
    }

    @Override
    public List<FocusStatisticsSessionView> findSessions(UserId userId, FocusStatisticsPeriod period) {
        return focusSessionRepository.findAllOverlappingPeriod(
                        userId,
                        period.startInstant(),
                        period.endInstant()
                ).stream()
                .map(this::toSessionView)
                .toList();
    }

    private FocusStatisticsSummaryView toSummaryView(DailyFocusSummaryView summary) {
        return new FocusStatisticsSummaryView(
                summary.date(),
                summary.totalSeconds(),
                summary.items().stream()
                        .map(this::toSummaryItemView)
                        .toList()
        );
    }

    private FocusStatisticsSummaryItemView toSummaryItemView(DailyFocusSummaryItemView item) {
        return new FocusStatisticsSummaryItemView(
                item.overlapStartedAt(),
                item.overlapEndedAt()
        );
    }

    private FocusStatisticsSessionView toSessionView(FocusSession focusSession) {
        return new FocusStatisticsSessionView(
                focusSession.getStartedAt(),
                focusSession.getEndedAt()
        );
    }
}
