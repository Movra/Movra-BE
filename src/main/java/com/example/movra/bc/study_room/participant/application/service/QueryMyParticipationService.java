package com.example.movra.bc.study_room.participant.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.application.service.dto.response.MyParticipationResponse;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryMyParticipationService {

    private final ParticipantRepository participantRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public List<MyParticipationResponse> query() {
        UserId userId = currentUserQuery.currentUser().userId();
        return participantRepository.findAllByUserId(userId).stream()
                .map(MyParticipationResponse::from)
                .toList();
    }
}
