package com.example.movra.bc.feedback.daily_reflection.application.service;

import com.example.movra.bc.feedback.daily_reflection.application.exception.DailyReflectionNotFoundException;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.request.UpdateDailyReflectionRequest;
import com.example.movra.bc.feedback.daily_reflection.domain.DailyReflection;
import com.example.movra.bc.feedback.daily_reflection.domain.repository.DailyReflectionRepository;
import com.example.movra.bc.feedback.daily_reflection.domain.vo.DailyReflectionId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateDailyReflectionService {

    private final DailyReflectionRepository dailyReflectionRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void update(UUID dailyReflectionId, UpdateDailyReflectionRequest request) {
        DailyReflection dailyReflection = dailyReflectionRepository.findByIdAndUserId(
                        DailyReflectionId.of(dailyReflectionId),
                        currentUserQuery.currentUser().userId()
                )
                .orElseThrow(DailyReflectionNotFoundException::new);

        dailyReflection.update(
                request.whatWentWell(),
                request.whatBrokeDown(),
                request.nextAction()
        );

        dailyReflectionRepository.save(dailyReflection);
    }
}
