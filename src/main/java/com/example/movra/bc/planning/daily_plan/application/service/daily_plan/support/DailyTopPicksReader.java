package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryItemView;
import com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.dto.DailyTopPicksSummaryView;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyTopPicksReader {

    private final DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;

    public List<DailyTopPicksSummaryView> findRange(UserId userId, LocalDate from, LocalDate to) {
        return dailyTopPicksSummaryRepository.findWithItemsByUserIdAndDateBetween(userId, from, to).stream()
                .map(this::toView)
                .toList();
    }

    public Optional<DailyTopPicksSummaryView> findOne(UserId userId, LocalDate date) {
        return dailyTopPicksSummaryRepository.findWithItemsByUserIdAndDate(userId, date)
                .map(this::toView);
    }

    private DailyTopPicksSummaryView toView(DailyTopPicksSummary summary) {
        List<DailyTopPicksSummaryItemView> items = summary.getItems().stream()
                .map(item -> new DailyTopPicksSummaryItemView(
                        item.getContentSnapshot(),
                        item.isCompletedSnapshot(),
                        item.getEstimatedMinutesSnapshot(),
                        item.getMemoSnapshot(),
                        item.getDisplayOrder()
                ))
                .toList();

        return new DailyTopPicksSummaryView(
                summary.getUserId(),
                summary.getDate(),
                summary.getTotalCount(),
                summary.getCompletedCount(),
                items
        );
    }
}
