package com.example.movra.bc.planning.exam_schedule.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.ExamScheduleResponse;
import com.example.movra.bc.planning.exam_schedule.application.service.dto.response.SeasonModeResponse;
import com.example.movra.bc.planning.exam_schedule.domain.repository.ExamScheduleRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class QuerySeasonModeService {

    private final ExamScheduleRepository examScheduleRepository;
    private final CurrentUserQuery currentUserQuery;
    private final Clock clock;

    @Transactional(readOnly = true)
    public SeasonModeResponse queryMine() {
        UserId userId = currentUserQuery.currentUser().userId();
        LocalDate today = LocalDate.now(clock);

        return examScheduleRepository.findFirstByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today)
                .map(examSchedule -> ExamScheduleResponse.from(examSchedule, today))
                .map(SeasonModeResponse::from)
                .orElseGet(SeasonModeResponse::baseline);
    }
}
