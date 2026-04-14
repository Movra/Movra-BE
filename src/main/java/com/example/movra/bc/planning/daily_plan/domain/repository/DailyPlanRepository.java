package com.example.movra.bc.planning.daily_plan.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, DailyPlanId> {

    Optional<DailyPlan> findByDailyPlanIdAndUserId(DailyPlanId dailyPlanId, UserId userId);

    Optional<DailyPlan> findByUserIdAndPlanDate(UserId userId, LocalDate planDate);

    boolean existsByUserIdAndPlanDate(UserId userId, LocalDate planDate);
}
