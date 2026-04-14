package com.example.movra.application.study_room.participant;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.application.service.QueryRoomParticipantsService;
import com.example.movra.bc.study_room.participant.application.service.dto.response.ParticipantResponse;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QueryRoomParticipantsServiceTest {

    @InjectMocks
    private QueryRoomParticipantsService queryRoomParticipantsService;

    @Mock
    private ParticipantRepository participantRepository;

    private final RoomId roomId = RoomId.newId();

    @Test
    @DisplayName("방 참여자 목록 조회 성공")
    void query_success() {
        // given
        Participant p1 = Participant.enter(UserId.newId(), roomId);
        Participant p2 = Participant.enter(UserId.newId(), roomId);
        given(participantRepository.findAllByRoomId(roomId)).willReturn(List.of(p1, p2));

        // when
        List<ParticipantResponse> responses = queryRoomParticipantsService.query(roomId.id());

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("참여자가 없으면 빈 리스트 반환")
    void query_noParticipants_returnsEmptyList() {
        // given
        given(participantRepository.findAllByRoomId(roomId)).willReturn(List.of());

        // when
        List<ParticipantResponse> responses = queryRoomParticipantsService.query(roomId.id());

        // then
        assertThat(responses).isEmpty();
    }
}
