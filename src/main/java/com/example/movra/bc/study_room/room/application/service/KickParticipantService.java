package com.example.movra.bc.study_room.room.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KickParticipantService {

    private final ParticipantRepository participantRepository;
    private final StudyRoomReader studyRoomReader;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void kick(UUID roomId, UUID targetUserId) {
        UserId leaderId = currentUserQuery.currentUser().userId();
        UserId targetId = UserId.of(targetUserId);
        Room room = studyRoomReader.getRoom(roomId);

        room.kick(leaderId, targetId);

        Participant target = studyRoomReader.getParticipant(targetId, room.getId());
        if (!target.isEnded()) {
            target.leaveAndRecordTime();
        }
        participantRepository.delete(target);
    }
}
