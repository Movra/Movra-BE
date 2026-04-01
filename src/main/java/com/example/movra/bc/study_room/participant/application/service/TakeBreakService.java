package com.example.movra.bc.study_room.participant.application.service;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TakeBreakService {

    private final StudyRoomReader studyRoomReader;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void takeBreak(UUID roomId) {
        UserId userId = currentUserQuery.currentUser().userId();
        Participant participant = studyRoomReader.getParticipant(userId, RoomId.of(roomId));
        participant.takeBreak();
    }
}
