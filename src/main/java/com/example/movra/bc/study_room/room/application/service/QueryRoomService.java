package com.example.movra.bc.study_room.room.application.service;

import com.example.movra.bc.study_room.helper.ParticipantProfileReader;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.application.service.dto.response.PublicRoomResponse;
import com.example.movra.bc.study_room.room.application.service.dto.response.RoomDetailResponse;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryRoomService {

    private final StudyRoomReader studyRoomReader;
    private final ParticipantRepository participantRepository;
    private final ParticipantProfileReader participantProfileReader;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public RoomDetailResponse query(UUID roomId) {
        Room room = studyRoomReader.getRoom(roomId);
        studyRoomReader.getParticipant(currentUserQuery.currentUser().userId(), room.getId());

        List<Participant> participants = participantRepository.findAllByRoomId(room.getId());
        Map<UUID, String> profileNameMap = participantProfileReader.getProfileNameMap(participants);

        return RoomDetailResponse.from(room, participants, profileNameMap);
    }

    @Transactional(readOnly = true)
    public List<PublicRoomResponse> queryAll() {
        return studyRoomReader.getPublicRooms().stream()
                .map(PublicRoomResponse::from)
                .toList();
    }
}
