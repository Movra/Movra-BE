package com.example.movra.bc.study_room.participant.presentation;

import com.example.movra.bc.study_room.participant.application.service.QueryMyParticipationService;
import com.example.movra.bc.study_room.participant.application.service.dto.response.MyParticipationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/my-participations")
@RequiredArgsConstructor
public class MyParticipationController {

    private final QueryMyParticipationService queryMyParticipationService;

    @GetMapping
    public List<MyParticipationResponse> query() {
        return queryMyParticipationService.query();
    }
}
