package com.example.movra.bc.study_room.participant.presentation;

import com.example.movra.bc.study_room.participant.application.service.QueryMyParticipationService;
import com.example.movra.bc.study_room.participant.application.service.QueryRoomParticipantsService;
import com.example.movra.bc.study_room.participant.application.service.StartFocusService;
import com.example.movra.bc.study_room.participant.application.service.TakeBreakService;
import com.example.movra.bc.study_room.participant.application.service.dto.response.MyParticipationResponse;
import com.example.movra.bc.study_room.participant.application.service.dto.response.ParticipantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms/{roomId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final StartFocusService startFocusService;
    private final TakeBreakService takeBreakService;
    private final QueryRoomParticipantsService queryRoomParticipantsService;

    @GetMapping
    public List<ParticipantResponse> queryAll(@PathVariable UUID roomId) {
        return queryRoomParticipantsService.query(roomId);
    }

    @PatchMapping("/focus")
    public void startFocus(@PathVariable UUID roomId) {
        startFocusService.start(roomId);
    }

    @PatchMapping("/break")
    public void takeBreak(@PathVariable UUID roomId) {
        takeBreakService.takeBreak(roomId);
    }
}
