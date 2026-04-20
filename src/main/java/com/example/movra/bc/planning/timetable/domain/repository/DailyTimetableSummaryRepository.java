package com.example.movra.bc.planning.timetable.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.timetable.domain.DailyTimetableSummary;
import com.example.movra.bc.planning.timetable.domain.vo.DailyTimetableSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyTimetableSummaryRepository extends JpaRepository<DailyTimetableSummary, DailyTimetableSummaryId> {

    boolean existsByUserIdAndDate(UserId userId, LocalDate date);

    Optional<DailyTimetableSummary> findByUserIdAndDate(UserId userId, LocalDate date);

    List<DailyTimetableSummary> findByUserIdAndDateBetween(UserId userId, LocalDate from, LocalDate to);

    @Query("""
        select distinct s
        from DailyTimetableSummary s
        left join fetch s.items i
        where s.userId = :userId
          and s.date = :date
        order by i.displayOrder asc
    """)
    Optional<DailyTimetableSummary> findWithItemsByUserIdAndDate(UserId userId, LocalDate date);

    @Query("""
        select distinct s
        from DailyTimetableSummary s
        left join fetch s.items i
        where s.userId = :userId
          and s.date between :from and :to
        order by s.date asc, i.displayOrder asc
    """)
    List<DailyTimetableSummary> findWithItemsByUserIdAndDateBetween(UserId userId, LocalDate from, LocalDate to);
}
