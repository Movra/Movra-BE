package com.example.movra.bc.study_room.room.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.application.service.dto.request.JoinRoomRequest;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.exception.AlreadyJoinedException;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JoinRoomService {

    private final ParticipantRepository participantRepository;
    private final StudyRoomReader studyRoomReader;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void join(JoinRoomRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        Room room = studyRoomReader.getRoomByInviteCode(request.inviteCode());

        if (participantRepository.existsByUserIdAndRoomId(userId, room.getId())) {
            throw new AlreadyJoinedException();
        }

        room.join(userId, request.inviteCode());

        try {
            Participant participant = Participant.enter(userId, room.getId());
            participantRepository.save(participant);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyJoinedException();
        }
    }
}
