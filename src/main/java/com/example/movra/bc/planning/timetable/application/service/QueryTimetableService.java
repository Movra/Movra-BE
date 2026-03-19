package com.example.movra.bc.planning.timetable.application.service;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.timetable.application.service.dto.response.TimetableResponse;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryTimetableService {

    private final TimetableRepository timetableRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public TimetableResponse findByDailyPlanId(UUID dailyPlanId) {
        DailyPlanId planId = DailyPlanId.of(dailyPlanId);

        DailyPlan dailyPlan = dailyPlanRepository.findByDailyPlanIdAndUserId(planId, currentUserQuery.currentUser().userId())
                .orElseThrow(DailyPlanNotFoundException::new);

        Timetable timetable = timetableRepository.findByDailyPlanId(planId)
                .orElseThrow(TimetableNotFoundException::new);

        return TimetableResponse.from(timetable, dailyPlan);
    }
}
