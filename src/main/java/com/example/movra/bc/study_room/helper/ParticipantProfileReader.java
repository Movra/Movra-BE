package com.example.movra.bc.study_room.helper;

import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.domain.Participant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantProfileReader {

    private final UserRepository userRepository;

    public Map<UUID, String> getProfileNameMap(List<Participant> participants) {
        if (participants.isEmpty()) {
            return Map.of();
        }

        List<UserId> userIds = participants.stream()
                .map(Participant::getUserId)
                .distinct()
                .toList();

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(user -> user.getId().id(), User::getProfileName));
    }
}
