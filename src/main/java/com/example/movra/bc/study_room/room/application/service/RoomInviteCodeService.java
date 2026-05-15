package com.example.movra.bc.study_room.room.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.room.application.service.dto.response.RoomInviteCodeResponse;
import com.example.movra.bc.study_room.room.domain.PrivateRoom;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.exception.InvalidInviteCodeException;
import com.example.movra.bc.study_room.room.domain.repository.RoomRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomInviteCodeService {

    private final StudyRoomReader studyRoomReader;
    private final RoomRepository roomRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public RoomInviteCodeResponse query(UUID roomId) {
        UserId userId = currentUserQuery.currentUser().userId();
        Room room = studyRoomReader.getRoom(roomId);
        validatePrivateLeader(room, userId);

        return RoomInviteCodeResponse.from(room.getInviteCode());
    }

    @Transactional
    public RoomInviteCodeResponse reissue(UUID roomId) {
        UserId userId = currentUserQuery.currentUser().userId();
        Room room = studyRoomReader.getRoom(roomId);
        validatePrivateLeader(room, userId);

        RoomInviteCodeResponse response = RoomInviteCodeResponse.from(
                room.reissueInviteCode(userId)
        );
        roomRepository.save(room);
        return response;
    }

    private void validatePrivateLeader(Room room, UserId userId) {
        if (!(room instanceof PrivateRoom)) {
            throw new InvalidInviteCodeException();
        }

        room.validateLeader(userId);
    }
}
