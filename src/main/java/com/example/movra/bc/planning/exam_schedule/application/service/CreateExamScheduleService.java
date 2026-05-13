package com.example.movra.bc.planning.exam_schedule.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.request.ExamScheduleRequest;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.ExamScheduleResponse;
import com.example.movra.bc.planning.exam_schedule.domain.ExamSchedule;
import com.example.movra.bc.planning.exam_schedule.domain.repository.ExamScheduleRepository;
import com.example.movra.config.cache.HomeCacheNames;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreateExamScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @CacheEvict(
            cacheNames = HomeCacheNames.NEXT_EXAM_SCHEDULE,
            key = "@homeCacheKey.currentUserIdToday()"
    )
    @Transactional
    public ExamScheduleResponse create(ExamScheduleRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        ExamSchedule examSchedule = examScheduleRepository.save(
                ExamSchedule.create(
                        userId,
                        request.examType(),
                        request.title(),
                        request.examDate(),
                        request.subject(),
                        clock
                )
        );
        analyticsEventRecorder.recordSafely(
                userId,
                AnalyticsEventType.EXAM_REGISTERED,
                analyticsProperties(examSchedule)
        );

        return ExamScheduleResponse.from(examSchedule, LocalDate.now(clock));
    }

    private Map<String, String> analyticsProperties(ExamSchedule examSchedule) {
        Map<String, String> properties = new HashMap<>();
        properties.put("examScheduleId", examSchedule.getExamScheduleId().id().toString());
        properties.put("examType", examSchedule.getExamType().name());
        properties.put("examDate", examSchedule.getExamDate().toString());
        if (examSchedule.getSubject() != null) {
            properties.put("subject", examSchedule.getSubject());
        }
        return properties;
    }
}
