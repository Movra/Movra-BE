package com.example.movra.bc.planning.timetable.application.event;

import com.example.movra.bc.planning.daily_plan.domain.event.TaskTopPickedEvent;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TaskTopPickedEventHandler {

    private final TimetableRepository timetableRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void handle(TaskTopPickedEvent event) {
        Timetable timetable = timetableRepository.findByDailyPlanId(event.dailyPlanId())
                .orElseThrow(TimetableNotFoundException::new);

        timetable.onTopPickSelected(event.taskId());

        timetableRepository.save(timetable);
    }
}
