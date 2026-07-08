package com.example.movra.bc.insight.behavior_insight.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.insight.behavior_insight.domain.InsightReport;
import com.example.movra.bc.insight.behavior_insight.domain.vo.InsightReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsightReportRepository extends JpaRepository<InsightReport, InsightReportId> {

    Optional<InsightReport> findFirstByUserIdOrderByPeriod_PeriodEndDesc(UserId userId);

    Optional<InsightReport> findByUserIdAndPeriod_PeriodStart(UserId userId, LocalDate periodStart);

    boolean existsByUserIdAndPeriod_PeriodStart(UserId userId, LocalDate periodStart);

    List<InsightReport> findAllByUserIdOrderByPeriod_PeriodEndDesc(UserId userId);
}
