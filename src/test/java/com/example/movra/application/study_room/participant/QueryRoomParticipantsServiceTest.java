package com.example.movra.application.study_room.participant;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.ParticipantProfileReader;
import com.example.movra.bc.study_room.participant.application.exception.ParticipantNotFoundException;
import com.example.movra.bc.study_room.participant.application.service.QueryRoomParticipantsService;
import com.example.movra.bc.study_room.participant.application.service.dto.response.ParticipantResponse;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QueryRoomParticipantsServiceTest {

    private void givenCurrentUser(UserId userId) {
        given(currentUserQuery.currentUser()).willReturn(
                AuthenticatedUser.builder()
                        .userId(userId)
                        .accountId("account")
                        .name("user")
                        .build()
        );
    }

    @InjectMocks
    private QueryRoomParticipantsService queryRoomParticipantsService;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private ParticipantProfileReader participantProfileReader;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final RoomId roomId = RoomId.newId();

    @Test
    @DisplayName("방 참여자 목록 조회 성공")
    void query_success() {
        // given
        UserId userId1 = UserId.newId();
        UserId userId2 = UserId.newId();
        Participant p1 = Participant.enter(userId1, roomId);
        Participant p2 = Participant.enter(userId2, roomId);
        List<Participant> participants = List.of(p1, p2);

        givenCurrentUser(userId1);
        given(participantRepository.existsByUserIdAndRoomId(userId1, roomId)).willReturn(true);
        given(participantRepository.findAllByRoomId(roomId)).willReturn(participants);
        given(participantProfileReader.getProfileNameMap(participants)).willReturn(Map.of(
                userId1.id(), "참여자1",
                userId2.id(), "참여자2"
        ));

        // when
        List<ParticipantResponse> responses = queryRoomParticipantsService.query(roomId.id());

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting("participantName")
                .containsExactly("참여자1", "참여자2");
    }

    @Test
    @DisplayName("참여자가 없으면 빈 리스트 반환")
    void query_noParticipants_returnsEmptyList() {
        // given
        UserId userId = UserId.newId();
        givenCurrentUser(userId);
        given(participantRepository.existsByUserIdAndRoomId(userId, roomId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> queryRoomParticipantsService.query(roomId.id()))
                .isInstanceOf(ParticipantNotFoundException.class);
    }
}
