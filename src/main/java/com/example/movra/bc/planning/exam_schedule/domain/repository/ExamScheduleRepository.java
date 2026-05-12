package com.example.movra.bc.planning.exam_schedule.domain.repository;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.vo.ExamScheduleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, ExamScheduleId> {

    Optional<ExamSchedule> findByExamScheduleIdAndUserId(ExamScheduleId examScheduleId, UserId userId);

    List<ExamSchedule> findAllByUserIdOrderByExamDateAsc(UserId userId);

    Optional<ExamSchedule> findFirstByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(UserId userId, LocalDate examDate);

    Optional<ExamSchedule> findFirstByUserIdAndExamDateBetweenOrderByExamDateDesc(
            UserId userId,
            LocalDate from,
            LocalDate to
    );

    List<ExamSchedule> findAllByExamDateIn(Collection<LocalDate> examDates);
}
