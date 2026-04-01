package com.example.movra.bc.study_room.room.application.service;

import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.room.application.service.dto.response.RoomDetailResponse;
import com.example.movra.bc.study_room.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryRoomService {

    private final StudyRoomReader studyRoomReader;
    private final ParticipantRepository participantRepository;

    @Transactional(readOnly = true)
    public RoomDetailResponse query(UUID roomId) {
        Room room = studyRoomReader.getRoom(roomId);
        List<Participant> participants = participantRepository.findAllByRoomId(room.getId());
        return RoomDetailResponse.from(room, participants);
    }
}
