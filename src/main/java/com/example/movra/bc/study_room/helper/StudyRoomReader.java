package com.example.movra.bc.study_room.helper;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.application.exception.ParticipantNotFoundException;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.application.exception.RoomNotFoundException;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.repository.RoomRepository;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRoomReader {

    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;

    public Room getRoom(UUID roomId) {
        return roomRepository.findById(RoomId.of(roomId))
                .orElseThrow(RoomNotFoundException::new);
    }

    public Participant getParticipant(UserId userId, RoomId roomId) {
        return participantRepository.findByUserIdAndRoomId(userId, roomId)
                .orElseThrow(ParticipantNotFoundException::new);
    }
}
