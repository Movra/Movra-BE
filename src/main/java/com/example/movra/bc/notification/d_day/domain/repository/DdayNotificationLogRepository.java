package com.example.movra.bc.notification.d_day.domain.repository;

import com.example.movra.bc.notification.d_day.domain.DdayNotificationLog;
import com.example.movra.bc.notification.d_day.domain.vo.DdayNotificationLogId;
import com.example.movra.bc.planning.exam_schedule.domain.vo.ExamScheduleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DdayNotificationLogRepository extends JpaRepository<DdayNotificationLog, DdayNotificationLogId> {

    boolean existsByExamScheduleIdAndMilestoneDays(ExamScheduleId examScheduleId, int milestoneDays);
}
