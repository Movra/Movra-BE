package com.example.movra.bc.study_room.participant.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.application.exception.ParticipantNotFoundException;
import com.example.movra.bc.study_room.helper.ParticipantProfileReader;
import com.example.movra.bc.study_room.participant.application.service.dto.response.ParticipantResponse;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryRoomParticipantsService {

    private final ParticipantRepository participantRepository;
    private final ParticipantProfileReader participantProfileReader;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public List<ParticipantResponse> query(UUID roomId) {
        UserId userId = currentUserQuery.currentUser().userId();
        RoomId targetRoomId = RoomId.of(roomId);
        if (!participantRepository.existsByUserIdAndRoomId(userId, targetRoomId)) {
            throw new ParticipantNotFoundException();
        }

        List<Participant> participants = participantRepository.findAllByRoomId(targetRoomId);
        Map<UUID, String> profileNameMap = participantProfileReader.getProfileNameMap(participants);

        return participants.stream()
                .map(participant -> ParticipantResponse.from(
                        participant,
                        profileNameMap.getOrDefault(participant.getUserId().id(), "")
                ))
                .toList();
    }
}
