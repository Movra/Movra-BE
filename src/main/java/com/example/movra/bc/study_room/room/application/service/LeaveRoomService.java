package com.example.movra.bc.study_room.room.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.repository.RoomRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveRoomService {

    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final StudyRoomReader studyRoomReader;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void leave(UUID roomId) {
        UserId userId = currentUserQuery.currentUser().userId();
        Room room = studyRoomReader.getRoom(roomId);
        Participant participant = studyRoomReader.getParticipant(userId, room.getId());
        boolean isLeader = userId.equals(room.getLeaderId());

        participant.leaveAndRecordTime();
        participantRepository.delete(participant);

        boolean hasRemaining = participantRepository.existsByRoomId(room.getId());

        if (!hasRemaining) {
            room.dissolve();
            roomRepository.delete(room);
        } else if (isLeader) {
            Participant nextLeader = participantRepository
                    .findFirstByRoomIdOrderByJoinedAtAsc(room.getId())
                    .orElseThrow();
            room.reassignLeader(nextLeader.getUserId());
            roomRepository.save(room);
        }
    }
}
