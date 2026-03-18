package com.example.movra.bc.planning.daily_plan.application.event;

import com.example.movra.bc.planning.daily_plan.application.exception.DailyPlanNotFoundException;
import com.example.movra.bc.planning.daily_plan.domain.DailyPlan;
import com.example.movra.bc.planning.daily_plan.domain.repository.DailyPlanRepository;
import com.example.movra.bc.planning.timetable.domain.event.SlotRescheduledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SlotRescheduledEventHandler {

    private final DailyPlanRepository dailyPlanRepository;


    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void handle(SlotRescheduledEvent event){
        DailyPlan dailyPlan = dailyPlanRepository.findById(event.dailyPlanId())
                .orElseThrow(DailyPlanNotFoundException::new);

        dailyPlan.updateEstimatedMinutes(event.taskId(), event.newEstimatedMinutes());

        dailyPlanRepository.save(dailyPlan);
    }
}
