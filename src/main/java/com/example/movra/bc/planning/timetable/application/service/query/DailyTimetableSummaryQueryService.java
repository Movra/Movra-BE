package com.example.movra.bc.planning.timetable.application.service.query;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.timetable.application.service.query.dto.DailyTimetableSummaryView;
import com.example.movra.bc.planning.timetable.domain.repository.DailyTimetableSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyTimetableSummaryQueryService {

    private final DailyTimetableSummaryRepository dailyTimetableSummaryRepository;

    public List<DailyTimetableSummaryView> findRange(UserId userId, LocalDate from, LocalDate to) {
        return dailyTimetableSummaryRepository.findByUserIdAndDateBetween(userId, from, to).stream()
                .map(s -> new DailyTimetableSummaryView(s.getUserId(), s.getDate(), s.getTotalCount(), s.getCompletedCount()))
                .toList();
    }

    public Optional<DailyTimetableSummaryView> findOne(UserId userId, LocalDate date) {
        return dailyTimetableSummaryRepository.findByUserIdAndDate(userId, date)
                .map(s -> new DailyTimetableSummaryView(s.getUserId(), s.getDate(), s.getTotalCount(), s.getCompletedCount()));
    }
}
