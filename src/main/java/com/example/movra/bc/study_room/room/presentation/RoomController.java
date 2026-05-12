package com.example.movra.bc.study_room.room.presentation;

import com.example.movra.bc.study_room.room.application.service.CreateRoomService;
import com.example.movra.bc.study_room.room.application.service.JoinRoomService;
import com.example.movra.bc.study_room.room.application.service.KickParticipantService;
import com.example.movra.bc.study_room.room.application.service.LeaveRoomService;
import com.example.movra.bc.study_room.room.application.service.QueryRoomService;
import com.example.movra.bc.study_room.room.application.service.dto.request.CreateRoomRequest;
import com.example.movra.bc.study_room.room.application.service.dto.request.JoinRoomRequest;
import com.example.movra.bc.study_room.room.application.service.dto.response.CreateRoomResponse;
import com.example.movra.bc.study_room.room.application.service.dto.response.PublicRoomResponse;
import com.example.movra.bc.study_room.room.application.service.dto.response.RoomDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final CreateRoomService createRoomService;
    private final JoinRoomService joinRoomService;
    private final LeaveRoomService leaveRoomService;
    private final KickParticipantService kickParticipantService;
    private final QueryRoomService queryRoomService;

    @PostMapping
    public CreateRoomResponse create(@Valid @RequestBody CreateRoomRequest request) {
        return createRoomService.create(request);
    }

    @GetMapping
    public List<PublicRoomResponse> queryAll() {
        return queryRoomService.queryAll();
    }

    @GetMapping("/{roomId}")
    public RoomDetailResponse query(@PathVariable UUID roomId) {
        return queryRoomService.query(roomId);
    }

    @PostMapping("/join")
    public void join(@Valid @RequestBody JoinRoomRequest request) {
        joinRoomService.join(request);
    }

    @PostMapping("/{roomId}/leave")
    public void leave(@PathVariable UUID roomId) {
        leaveRoomService.leave(roomId);
    }

    @DeleteMapping("/{roomId}/participants/{targetUserId}")
    public void kick(@PathVariable UUID roomId, @PathVariable UUID targetUserId) {
        kickParticipantService.kick(roomId, targetUserId);
    }
}
