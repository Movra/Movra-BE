package com.example.movra.bc.planning.timetable.presentation;

import com.example.movra.bc.planning.timetable.application.service.QueryTimetableService;
import com.example.movra.bc.planning.timetable.application.service.dto.response.TimetableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/timetables")
@RequiredArgsConstructor
public class TimetableController {

    private final QueryTimetableService queryTimetableService;

    @GetMapping
    public TimetableResponse findByDailyPlanId(@RequestParam UUID dailyPlanId) {
        return queryTimetableService.findByDailyPlanId(dailyPlanId);
    }
}
