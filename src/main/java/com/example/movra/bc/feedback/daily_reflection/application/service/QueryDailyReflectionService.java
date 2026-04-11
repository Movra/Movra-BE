package com.example.movra.bc.feedback.daily_reflection.application.service;

import com.example.movra.bc.feedback.daily_reflection.application.exception.DailyReflectionNotFoundException;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.response.DailyReflectionResponse;
import com.example.movra.bc.feedback.daily_reflection.domain.repository.DailyReflectionRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class QueryDailyReflectionService {

    private final DailyReflectionRepository dailyReflectionRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public DailyReflectionResponse query(LocalDate targetDate) {
        return dailyReflectionRepository.findByUserIdAndReflectionDate(
                        currentUserQuery.currentUser().userId(),
                        targetDate
                )
                .map(DailyReflectionResponse::from)
                .orElseThrow(DailyReflectionNotFoundException::new);
    }
}
