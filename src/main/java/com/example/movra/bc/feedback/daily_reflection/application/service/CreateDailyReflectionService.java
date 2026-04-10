package com.example.movra.bc.feedback.daily_reflection.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.feedback.daily_reflection.application.exception.DailyReflectionAlreadyExistsException;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.request.CreateDailyReflectionRequest;
import com.example.movra.bc.feedback.daily_reflection.domain.DailyReflection;
import com.example.movra.bc.feedback.daily_reflection.domain.repository.DailyReflectionRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateDailyReflectionService {

    private final DailyReflectionRepository dailyReflectionRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void create(CreateDailyReflectionRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();

        if (dailyReflectionRepository.existsByUserIdAndReflectionDate(userId, request.reflectionDate())) {
            throw new DailyReflectionAlreadyExistsException();
        }

        try {
            dailyReflectionRepository.saveAndFlush(
                    DailyReflection.create(
                            userId,
                            request.reflectionDate(),
                            request.whatWentWell(),
                            request.whatBrokeDown(),
                            request.nextAction()
                    )
            );
        } catch (DataIntegrityViolationException e) {
            throw new DailyReflectionAlreadyExistsException();
        }
    }
}
