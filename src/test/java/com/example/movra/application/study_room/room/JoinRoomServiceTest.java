package com.example.movra.application.study_room.room;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.application.service.JoinRoomService;
import com.example.movra.bc.study_room.room.application.service.dto.request.JoinRoomRequest;
import com.example.movra.bc.study_room.room.domain.PrivateRoom;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.exception.AlreadyJoinedException;
import com.example.movra.bc.study_room.room.domain.exception.InvalidInviteCodeException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JoinRoomServiceTest {

    @InjectMocks
    private JoinRoomService joinRoomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private StudyRoomReader studyRoomReader;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();
    private final UserId leaderId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("공개 방 입장 성공")
    void join_publicRoom_success() {
        // given
        givenCurrentUser();
        Room room = Room.create("스터디룸", leaderId, Visibility.PUBLIC);
        given(studyRoomReader.getRoom(any())).willReturn(room);
        given(participantRepository.existsByUserIdAndRoomId(userId, room.getId())).willReturn(false);

        // when
        joinRoomService.join(room.getId().id(), new JoinRoomRequest(null));

        // then
        then(participantRepository).should().save(any());
    }

    @Test
    @DisplayName("비공개 방 초대 코드로 입장 성공")
    void join_privateRoomWithInviteCode_success() {
        // given
        givenCurrentUser();
        PrivateRoom room = PrivateRoom.create("비공개룸", leaderId);
        String inviteCode = room.getInviteCode().code();
        given(studyRoomReader.getRoom(any())).willReturn(room);
        given(participantRepository.existsByUserIdAndRoomId(userId, room.getId())).willReturn(false);

        // when
        joinRoomService.join(room.getId().id(), new JoinRoomRequest(inviteCode));

        // then
        then(participantRepository).should().save(any());
    }

    @Test
    @DisplayName("이미 참여 중인 방에 입장 시 AlreadyJoinedException 발생")
    void join_alreadyJoined_throwsException() {
        // given
        givenCurrentUser();
        Room room = Room.create("스터디룸", leaderId, Visibility.PUBLIC);
        given(studyRoomReader.getRoom(any())).willReturn(room);
        given(participantRepository.existsByUserIdAndRoomId(userId, room.getId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> joinRoomService.join(room.getId().id(), new JoinRoomRequest(null)))
                .isInstanceOf(AlreadyJoinedException.class);
    }

    @Test
    @DisplayName("비공개 방에 잘못된 초대 코드로 입장 시 InvalidInviteCodeException 발생")
    void join_invalidInviteCode_throwsException() {
        // given
        givenCurrentUser();
        PrivateRoom room = PrivateRoom.create("비공개룸", leaderId);
        given(studyRoomReader.getRoom(any())).willReturn(room);
        given(participantRepository.existsByUserIdAndRoomId(userId, room.getId())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> joinRoomService.join(room.getId().id(), new JoinRoomRequest("wrong-code")))
                .isInstanceOf(InvalidInviteCodeException.class);
    }
}
