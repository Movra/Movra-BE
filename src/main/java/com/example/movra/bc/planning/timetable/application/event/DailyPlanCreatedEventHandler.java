package com.example.movra.bc.planning.timetable.application.event;

import com.example.movra.bc.planning.daily_plan.domain.event.DailyPlanCreatedEvent;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DailyPlanCreatedEventHandler {

    private final TimetableRepository timetableRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(DailyPlanCreatedEvent event) {
        Timetable timetable = Timetable.create(event.dailyPlanId(), 0);
        timetableRepository.save(timetable);
    }
}
