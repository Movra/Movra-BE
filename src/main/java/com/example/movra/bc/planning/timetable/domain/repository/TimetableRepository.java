package com.example.movra.bc.planning.timetable.domain.repository;

import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, TimetableId> {

    Optional<Timetable> findByDailyPlanId(DailyPlanId dailyPlanId);
}
