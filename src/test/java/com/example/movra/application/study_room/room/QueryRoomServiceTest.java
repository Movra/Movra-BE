package com.example.movra.application.study_room.room;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.ParticipantProfileReader;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.application.service.QueryRoomService;
import com.example.movra.bc.study_room.room.application.service.dto.response.PublicRoomResponse;
import com.example.movra.bc.study_room.room.application.service.dto.response.RoomDetailResponse;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.vo.Visibility;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QueryRoomServiceTest {

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
    private QueryRoomService queryRoomService;

    @Mock
    private StudyRoomReader studyRoomReader;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private ParticipantProfileReader participantProfileReader;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Test
    @DisplayName("방 상세 조회 성공")
    void query_success() {
        // given
        UserId leaderId = UserId.newId();
        Room room = Room.create("스터디룸", leaderId, Visibility.PUBLIC);
        UUID roomUuid = room.getId().id();

        Participant participant1 = Participant.enter(leaderId, room.getId());
        UserId participantId = UserId.newId();
        Participant participant2 = Participant.enter(participantId, room.getId());
        List<Participant> participants = List.of(participant1, participant2);

        given(studyRoomReader.getRoom(roomUuid)).willReturn(room);
        givenCurrentUser(leaderId);
        given(studyRoomReader.getParticipant(leaderId, room.getId())).willReturn(participant1);
        given(participantRepository.findAllByRoomId(room.getId())).willReturn(participants);
        given(participantProfileReader.getProfileNameMap(participants)).willReturn(Map.of(
                leaderId.id(), "방장",
                participantId.id(), "참여자"
        ));

        // when
        RoomDetailResponse response = queryRoomService.query(roomUuid);

        // then
        assertThat(response.roomId()).isEqualTo(roomUuid);
        assertThat(response.name()).isEqualTo("스터디룸");
        assertThat(response.leaderUserId()).isEqualTo(leaderId.id());
        assertThat(response.currentCount()).isEqualTo(2);
        assertThat(response.participants()).hasSize(2);
        assertThat(response.participants())
                .extracting("participantName")
                .containsExactly("방장", "참여자");
    }

    @Test
    @DisplayName("공개 방 목록 조회 성공")
    void queryAll_success() {
        // given
        Room room1 = Room.create("스터디룸1", UserId.newId(), Visibility.PUBLIC);
        Room room2 = Room.create("스터디룸2", UserId.newId(), Visibility.PUBLIC);

        given(studyRoomReader.getPublicRooms()).willReturn(List.of(room1, room2));

        // when
        List<PublicRoomResponse> responses = queryRoomService.queryAll();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting("roomId")
                .containsExactly(room1.getId().id(), room2.getId().id());
        assertThat(responses)
                .extracting("name")
                .containsExactly("스터디룸1", "스터디룸2");
    }
}
