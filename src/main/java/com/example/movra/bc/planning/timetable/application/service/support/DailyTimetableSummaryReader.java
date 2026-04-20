package com.example.movra.bc.planning.timetable.application.service.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryItemView;
import com.example.movra.bc.planning.timetable.application.service.support.dto.DailyTimetableSummaryView;
import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.repository.DailyTimetableSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyTimetableSummaryReader {

    private final DailyTimetableSummaryRepository dailyTimetableSummaryRepository;

    public List<DailyTimetableSummaryView> findRange(UserId userId, LocalDate from, LocalDate to) {
        return dailyTimetableSummaryRepository.findWithItemsByUserIdAndDateBetween(userId, from, to).stream()
                .map(this::toView)
                .toList();
    }

    public Optional<DailyTimetableSummaryView> findOne(UserId userId, LocalDate date) {
        return dailyTimetableSummaryRepository.findWithItemsByUserIdAndDate(userId, date)
                .map(this::toView);
    }

    private DailyTimetableSummaryView toView(DailyTimetableSummary summary) {
        List<DailyTimetableSummaryItemView> items = summary.getItems().stream()
                .map(item -> new DailyTimetableSummaryItemView(
                        item.getContentSnapshot(),
                        item.isCompletedSnapshot(),
                        item.getStartTimeSnapshot(),
                        item.getEndTimeSnapshot(),
                        item.isTopPickSnapshot(),
                        item.getDisplayOrder()
                ))
                .toList();

        return new DailyTimetableSummaryView(
                summary.getUserId(),
                summary.getDate(),
                summary.getTotalCount(),
                summary.getCompletedCount(),
                items
        );
    }
}
