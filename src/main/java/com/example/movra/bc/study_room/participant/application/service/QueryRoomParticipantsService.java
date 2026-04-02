package com.example.movra.bc.study_room.participant.application.service;

import com.example.movra.bc.study_room.participant.application.service.dto.response.ParticipantResponse;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryRoomParticipantsService {

    private final ParticipantRepository participantRepository;

    @Transactional(readOnly = true)
    public List<ParticipantResponse> query(UUID roomId) {
        return participantRepository.findAllByRoomId(RoomId.of(roomId)).stream()
                .map(ParticipantResponse::from)
                .toList();
    }
}
