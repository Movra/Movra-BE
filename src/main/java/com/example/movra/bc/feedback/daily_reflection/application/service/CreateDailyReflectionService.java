package com.example.movra.bc.feedback.daily_reflection.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.analytics.activation_event.application.service.AnalyticsEventRecorder;
import com.example.movra.bc.analytics.activation_event.domain.type.AnalyticsEventType;
import com.example.movra.bc.feedback.daily_reflection.application.exception.DailyReflectionAlreadyExistsException;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.request.CreateDailyReflectionRequest;
import com.example.movra.bc.feedback.daily_reflection.domain.DailyReflection;
import com.example.movra.bc.feedback.daily_reflection.domain.repository.DailyReflectionRepository;
import com.example.movra.sharedkernel.exception.DataIntegrityViolationUtils;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreateDailyReflectionService {

    private final DailyReflectionRepository dailyReflectionRepository;
    private final CurrentUserQuery currentUserQuery;
    private final AnalyticsEventRecorder analyticsEventRecorder;

    @Transactional
    public void create(CreateDailyReflectionRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        if (dailyReflectionRepository.existsByUserIdAndReflectionDate(userId, request.reflectionDate())) {
            throw new DailyReflectionAlreadyExistsException();
        }

        try {
            DailyReflection dailyReflection = dailyReflectionRepository.saveAndFlush(
                    DailyReflection.create(
                            userId,
                            request.reflectionDate(),
                            request.whatWentWell(),
                            request.whatBrokeDown(),
                            request.ifCondition(),
                            request.thenAction()
                    )
            );
            analyticsEventRecorder.recordSafely(
                    userId,
                    AnalyticsEventType.DAILY_REFLECTION_CREATED,
                    Map.of(
                            "dailyReflectionId", dailyReflection.getId().id().toString(),
                            "reflectionDate", request.reflectionDate().toString()
                    )
            );
        } catch (DataIntegrityViolationException e) {
            if (DataIntegrityViolationUtils.isDuplicateKeyViolation(e)) {
                throw new DailyReflectionAlreadyExistsException();
            }
            throw e;
        }
    }
}
