package com.example.movra.bc.planning.daily_plan.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyTopPicksSummary;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyTopPicksSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyTopPicksSummaryRepository extends JpaRepository<DailyTopPicksSummary, DailyTopPicksSummaryId> {

    boolean existsByUserIdAndDate(UserId userId, LocalDate date);

    Optional<DailyTopPicksSummary> findByUserIdAndDate(UserId userId, LocalDate date);

    List<DailyTopPicksSummary> findByUserIdAndDateBetween(UserId userId, LocalDate from, LocalDate to);
}
