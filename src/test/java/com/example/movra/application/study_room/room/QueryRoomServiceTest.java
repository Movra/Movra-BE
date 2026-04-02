package com.example.movra.application.study_room.room;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.application.service.QueryRoomService;
import com.example.movra.bc.study_room.room.application.service.dto.response.RoomDetailResponse;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.vo.Visibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QueryRoomServiceTest {

    @InjectMocks
    private QueryRoomService queryRoomService;

    @Mock
    private StudyRoomReader studyRoomReader;

    @Mock
    private ParticipantRepository participantRepository;

    @Test
    @DisplayName("방 상세 조회 성공")
    void query_success() {
        // given
        UserId leaderId = UserId.newId();
        Room room = Room.create("스터디룸", leaderId, Visibility.PUBLIC);
        UUID roomUuid = room.getId().id();

        Participant participant1 = Participant.enter(leaderId, room.getId());
        Participant participant2 = Participant.enter(UserId.newId(), room.getId());

        given(studyRoomReader.getRoom(roomUuid)).willReturn(room);
        given(participantRepository.findAllByRoomId(room.getId())).willReturn(List.of(participant1, participant2));

        // when
        RoomDetailResponse response = queryRoomService.query(roomUuid);

        // then
        assertThat(response.roomId()).isEqualTo(roomUuid);
        assertThat(response.name()).isEqualTo("스터디룸");
        assertThat(response.leaderUserId()).isEqualTo(leaderId.id());
        assertThat(response.currentCount()).isEqualTo(2);
        assertThat(response.participants()).hasSize(2);
    }
}
