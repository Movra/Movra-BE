package com.example.movra.application.study_room.room;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.application.service.LeaveRoomService;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.repository.RoomRepository;
import com.example.movra.bc.study_room.room.domain.vo.Visibility;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LeaveRoomServiceTest {

    @InjectMocks
    private LeaveRoomService leaveRoomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private StudyRoomReader studyRoomReader;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("마지막 참여자가 퇴장하면 방 삭제")
    void leave_lastParticipant_dissolvesRoom() {
        // given
        givenCurrentUser();
        Room room = Room.create("스터디룸", userId, Visibility.PUBLIC);
        Participant participant = Participant.enter(userId, room.getId());
        UUID roomUuid = room.getId().id();

        given(studyRoomReader.getRoom(roomUuid)).willReturn(room);
        given(studyRoomReader.getParticipant(userId, room.getId())).willReturn(participant);
        given(participantRepository.existsByRoomId(room.getId())).willReturn(false);

        // when
        leaveRoomService.leave(roomUuid);

        // then
        then(participantRepository).should().delete(participant);
        then(roomRepository).should().delete(room);
        then(roomRepository).should(never()).save(room);
    }

    @Test
    @DisplayName("일반 참여자가 퇴장하면 방 유지")
    void leave_nonLeader_roomRemains() {
        // given
        givenCurrentUser();
        UserId leaderId = UserId.newId();
        Room room = Room.create("스터디룸", leaderId, Visibility.PUBLIC);
        Participant participant = Participant.enter(userId, room.getId());
        UUID roomUuid = room.getId().id();

        given(studyRoomReader.getRoom(roomUuid)).willReturn(room);
        given(studyRoomReader.getParticipant(userId, room.getId())).willReturn(participant);
        given(participantRepository.existsByRoomId(room.getId())).willReturn(true);

        // when
        leaveRoomService.leave(roomUuid);

        // then
        then(participantRepository).should().delete(participant);
        then(roomRepository).should(never()).delete(room);
        then(roomRepository).should(never()).save(room);
    }

    @Test
    @DisplayName("리더가 퇴장하면 다음 참여자에게 리더 승계")
    void leave_leader_reassignsLeader() {
        // given
        givenCurrentUser();
        Room room = Room.create("스터디룸", userId, Visibility.PUBLIC);
        Participant participant = Participant.enter(userId, room.getId());
        UUID roomUuid = room.getId().id();

        UserId nextLeaderId = UserId.newId();
        Participant nextLeader = Participant.enter(nextLeaderId, room.getId());

        given(studyRoomReader.getRoom(roomUuid)).willReturn(room);
        given(studyRoomReader.getParticipant(userId, room.getId())).willReturn(participant);
        given(participantRepository.existsByRoomId(room.getId())).willReturn(true);
        given(participantRepository.findFirstByRoomIdOrderByJoinedAtAsc(room.getId()))
                .willReturn(Optional.of(nextLeader));

        // when
        leaveRoomService.leave(roomUuid);

        // then
        then(participantRepository).should().delete(participant);
        then(roomRepository).should().save(room);
    }
}
