package com.example.movra.bc.planning.timetable.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.vo.DailyTimetableSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyTimetableSummaryRepository extends JpaRepository<DailyTimetableSummary, DailyTimetableSummaryId> {

    boolean existsByUserIdAndDate(UserId userId, LocalDate date);

    Optional<DailyTimetableSummary> findByUserIdAndDate(UserId userId, LocalDate date);

    List<DailyTimetableSummary> findByUserIdAndDateBetween(UserId userId, LocalDate from, LocalDate to);
}
