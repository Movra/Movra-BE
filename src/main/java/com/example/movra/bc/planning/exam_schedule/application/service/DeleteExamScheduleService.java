package com.example.movra.bc.planning.exam_schedule.application.service;

import com.example.movra.bc.planning.exam_schedule.application.exception.ExamScheduleNotFoundException;
import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.repository.ExamScheduleRepository;
import com.example.movra.bc.planning.exam_schedule.domain.vo.ExamScheduleId;
import com.example.movra.config.cache.HomeCacheNames;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteExamScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final CurrentUserQuery currentUserQuery;

    @CacheEvict(
            cacheNames = HomeCacheNames.NEXT_EXAM_SCHEDULE,
            key = "@homeCacheKey.currentUserIdToday()"
    )
    @Transactional
    public void delete(UUID examScheduleId) {
        ExamSchedule examSchedule = examScheduleRepository.findByExamScheduleIdAndUserId(
                        ExamScheduleId.of(examScheduleId),
                        currentUserQuery.currentUser().userId()
                )
                .orElseThrow(ExamScheduleNotFoundException::new);

        examScheduleRepository.delete(examSchedule);
    }
}
