package com.example.movra.bc.planning.timetable.application.service;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AssignTaskSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignTaskSlotService {

    private final TimetableRepository timetableRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void assign(UUID timetableId, UUID taskId, AssignTaskSlotRequest request) {
        Timetable timetable = timetableRepository.findById(TimetableId.of(timetableId))
                .orElseThrow(TimetableNotFoundException::new);

        dailyPlanRepository.findByDailyPlanIdAndUserId(timetable.getDailyPlanId(), currentUserQuery.currentUser().userId())
                .orElseThrow(DailyPlanNotFoundException::new);

        timetable.assignTask(
                TaskId.of(taskId),
                request.startTime(),
                request.endTime()
        );

        timetableRepository.save(timetable);
    }
}
