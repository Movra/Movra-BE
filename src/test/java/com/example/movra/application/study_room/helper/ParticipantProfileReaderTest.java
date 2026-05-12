package com.example.movra.application.study_room.helper;

import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.domain.user.repository.UserRepository;
import com.example.movra.bc.study_room.helper.ParticipantProfileReader;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ParticipantProfileReaderTest {

    @InjectMocks
    private ParticipantProfileReader participantProfileReader;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("참여자 사용자 ID로 프로필 이름 맵 조회")
    void getProfileNameMap_success() {
        // given
        RoomId roomId = RoomId.newId();
        User user1 = User.createLocalUser("account1", "참여자1", "image1", "user1@test.com", "password");
        User user2 = User.createLocalUser("account2", "참여자2", "image2", "user2@test.com", "password");
        Participant participant1 = Participant.enter(user1.getId(), roomId);
        Participant participant2 = Participant.enter(user2.getId(), roomId);
        List<Participant> participants = List.of(participant1, participant2);

        given(userRepository.findAllById(List.of(user1.getId(), user2.getId())))
                .willReturn(List.of(user1, user2));

        // when
        Map<UUID, String> profileNameMap = participantProfileReader.getProfileNameMap(participants);

        // then
        assertThat(profileNameMap)
                .containsEntry(user1.getId().id(), "참여자1")
                .containsEntry(user2.getId().id(), "참여자2");
    }

    @Test
    @DisplayName("참여자가 없으면 사용자 조회 없이 빈 맵 반환")
    void getProfileNameMap_emptyParticipants_returnsEmptyMap() {
        // when
        Map<UUID, String> profileNameMap = participantProfileReader.getProfileNameMap(List.of());

        // then
        assertThat(profileNameMap).isEmpty();
        verifyNoInteractions(userRepository);
    }
}
