package com.example.movra.bc.planning.daily_plan.application.service.query;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.application.service.query.dto.DailyTopPicksSummaryView;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyTopPicksSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyTopPicksSummaryQueryService {

    private final DailyTopPicksSummaryRepository dailyTopPicksSummaryRepository;
    private final DailyPlanRepository dailyPlanRepository;

    public List<DailyTopPicksSummaryView> findRange(UserId userId, LocalDate from, LocalDate to) {
        return dailyTopPicksSummaryRepository.findByUserIdAndDateBetween(userId, from, to).stream()
                .map(s -> new DailyTopPicksSummaryView(s.getUserId(), s.getDate(), s.getTotalCount(), s.getCompletedCount()))
                .toList();
    }

    public Optional<DailyTopPicksSummaryView> findOne(UserId userId, LocalDate date) {
        return dailyTopPicksSummaryRepository.findByUserIdAndDate(userId, date)
                .map(s -> new DailyTopPicksSummaryView(s.getUserId(), s.getDate(), s.getTotalCount(), s.getCompletedCount()));
    }

    public List<UserId> findActiveUserIds(LocalDate date) {
        return dailyPlanRepository.findDistinctUserIdsByPlanDate(date);
    }
}
