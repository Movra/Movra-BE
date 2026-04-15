package com.example.movra.bc.focus.focus_session.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.focus.focus_session.domain.DailyFocusSummary;
import com.example.movra.bc.focus.focus_session.domain.vo.DailyFocusSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyFocusSummaryRepository extends JpaRepository<DailyFocusSummary, DailyFocusSummaryId> {

    boolean existsByUserIdAndDate(UserId userId, LocalDate date);

    Optional<DailyFocusSummary> findByUserIdAndDate(UserId userId, LocalDate date);

    List<DailyFocusSummary> findByUserIdAndDateBetween(UserId userId, LocalDate from, LocalDate to);
}
