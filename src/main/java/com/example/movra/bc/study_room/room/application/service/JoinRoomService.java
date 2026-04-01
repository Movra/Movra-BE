package com.example.movra.bc.study_room.room.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.application.service.dto.request.JoinRoomRequest;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.exception.AlreadyJoinedException;
import com.example.movra.bc.study_room.room.domain.repository.RoomRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JoinRoomService {

    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final StudyRoomReader studyRoomReader;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void join(UUID roomId, JoinRoomRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        Room room = studyRoomReader.getRoom(roomId);

        if (participantRepository.existsByUserIdAndRoomId(userId, room.getId())) {
            throw new AlreadyJoinedException();
        }

        room.join(userId, request.inviteCode());

        Participant participant = Participant.enter(userId, room.getId());
        participantRepository.save(participant);
    }
}
