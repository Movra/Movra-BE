package com.example.movra.bc.planning.timetable.application.service;

import com.example.movra.bc.planning.timetable.application.service.dto.request.RescheduleSlotRequest;
import com.example.movra.bc.planning.timetable.domain.Timetable;
import com.example.movra.bc.planning.timetable.domain.exception.TimetableNotFoundException;
import com.example.movra.bc.planning.timetable.domain.repository.TimetableRepository;
import com.example.movra.bc.planning.timetable.domain.vo.SlotId;
import com.example.movra.bc.planning.timetable.domain.vo.TimetableId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RescheduleSlotService {

    private final TimetableRepository timetableRepository;

    @Transactional
    public void reschedule(UUID timetableId, UUID slotId, RescheduleSlotRequest request) {
        Timetable timetable = timetableRepository.findById(TimetableId.of(timetableId))
                .orElseThrow(TimetableNotFoundException::new);

        timetable.reschedule(
                SlotId.of(slotId),
                request.startTime(),
                request.endTime()
        );

        timetableRepository.save(timetable);
    }
}
