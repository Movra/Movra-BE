package com.example.movra.application.study_room.participant;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.application.service.QueryMyParticipationService;
import com.example.movra.bc.study_room.participant.application.service.dto.response.MyParticipationResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QueryMyParticipationServiceTest {

    @InjectMocks
    private QueryMyParticipationService queryMyParticipationService;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("내 참여 목록 조회 성공")
    void query_success() {
        // given
        givenCurrentUser();
        Participant p1 = Participant.enter(userId, RoomId.newId());
        Participant p2 = Participant.enter(userId, RoomId.newId());
        given(participantRepository.findAllByUserId(userId)).willReturn(List.of(p1, p2));

        // when
        List<MyParticipationResponse> responses = queryMyParticipationService.query();

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("참여 중인 방이 없으면 빈 리스트 반환")
    void query_noParticipation_returnsEmptyList() {
        // given
        givenCurrentUser();
        given(participantRepository.findAllByUserId(userId)).willReturn(List.of());

        // when
        List<MyParticipationResponse> responses = queryMyParticipationService.query();

        // then
        assertThat(responses).isEmpty();
    }
}
