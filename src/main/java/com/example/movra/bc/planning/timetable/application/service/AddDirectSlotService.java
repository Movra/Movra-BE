package com.example.movra.bc.planning.timetable.application.service;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.DailyPlanId;
import com.example.movra.bc.planning.timetable.application.service.dto.request.AddDirectSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddDirectSlotService {

    private final DailyPlanRepository dailyPlanRepository;
    private final TimetableRepository timetableRepository;

    @Transactional
    public void execute(UUID timetableId, UUID dailyPlanId, AddDirectSlotRequest request) {
        DailyPlan dailyPlan = dailyPlanRepository.findById(DailyPlanId.of(dailyPlanId))
                .orElseThrow(DailyPlanNotFoundException::new);

        Task task = dailyPlan.addTask(request.content());

        Timetable timetable = timetableRepository.findById(TimetableId.of(timetableId))
                .orElseThrow(TimetableNotFoundException::new);

        timetable.assignTask(task.getTaskId(), request.startTime(), request.endTime());

        dailyPlanRepository.save(dailyPlan);
        timetableRepository.save(timetable);
    }
}
