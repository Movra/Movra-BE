package com.example.movra.bc.study_room.room.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.room.application.service.dto.request.CreateRoomRequest;
import com.example.movra.bc.study_room.room.application.service.dto.response.CreateRoomResponse;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.repository.RoomRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateRoomService {

    private final RoomRepository roomRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public CreateRoomResponse create(CreateRoomRequest request) {
        UserId userId = currentUserQuery.currentUser().userId();
        Room room = Room.create(request.name(), userId, request.visibility());
        roomRepository.save(room);
        return CreateRoomResponse.from(room);
    }
}
