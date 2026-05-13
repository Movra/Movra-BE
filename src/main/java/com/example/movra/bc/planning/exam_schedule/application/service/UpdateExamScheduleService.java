package com.example.movra.bc.planning.exam_schedule.application.service;

import com.example.movra.bc.planning.exam_schedule.application.exception.ExamScheduleNotFoundException;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.request.ExamScheduleRequest;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.ExamScheduleResponse;
import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.repository.ExamScheduleRepository;
import com.example.movra.bc.planning.exam_schedule.domain.vo.ExamScheduleId;
import com.example.movra.config.cache.HomeCacheNames;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateExamScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @CacheEvict(
            cacheNames = HomeCacheNames.NEXT_EXAM_SCHEDULE,
            key = "@homeCacheKey.currentUserIdToday()"
    )
    @Transactional
    public ExamScheduleResponse update(UUID examScheduleId, ExamScheduleRequest request) {
        ExamSchedule examSchedule = examScheduleRepository.findByExamScheduleIdAndUserId(
                        ExamScheduleId.of(examScheduleId),
                        currentUserQuery.currentUser().userId()
                )
                .orElseThrow(ExamScheduleNotFoundException::new);

        examSchedule.update(
                request.examType(),
                request.title(),
                request.examDate(),
                request.subject()
        );

        return ExamScheduleResponse.from(examScheduleRepository.save(examSchedule), LocalDate.now(clock));
    }
}
