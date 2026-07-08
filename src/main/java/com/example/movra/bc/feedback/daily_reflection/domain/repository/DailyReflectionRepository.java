package com.example.movra.bc.feedback.daily_reflection.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.feedback.daily_reflection.domain.DailyReflection;
import com.example.movra.bc.feedback.daily_reflection.domain.vo.DailyReflectionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyReflectionRepository extends JpaRepository<DailyReflection, DailyReflectionId> {

    boolean existsByUserIdAndReflectionDate(UserId userId, LocalDate reflectionDate);

    List<DailyReflection> findAllByUserIdAndReflectionDateBetween(UserId userId, LocalDate from, LocalDate to);

    Optional<DailyReflection> findByUserIdAndReflectionDate(UserId userId, LocalDate reflectionDate);

    Optional<DailyReflection> findByIdAndUserId(DailyReflectionId id, UserId userId);
}
