package com.example.movra.application.study_room.room;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.room.application.service.RoomInviteCodeService;
import com.example.movra.bc.study_room.room.application.service.dto.response.RoomInviteCodeResponse;
import com.example.movra.bc.study_room.room.domain.PrivateRoom;
import com.example.movra.bc.study_room.room.domain.PublicRoom;
import com.example.movra.bc.study_room.room.domain.exception.InvalidInviteCodeException;
import com.example.movra.bc.study_room.room.domain.exception.NotLeaderException;
import com.example.movra.bc.study_room.room.domain.repository.RoomRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RoomInviteCodeServiceTest {

    @InjectMocks
    private RoomInviteCodeService roomInviteCodeService;

    @Mock
    private StudyRoomReader studyRoomReader;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId leaderId = UserId.newId();
    private final UserId otherUserId = UserId.newId();

    private void givenCurrentUser(UserId userId) {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    void query_returnsPrivateRoomInviteCodeForLeader() {
        givenCurrentUser(leaderId);
        PrivateRoom room = PrivateRoom.create("private", leaderId);
        given(studyRoomReader.getRoom(room.getId().id())).willReturn(room);

        RoomInviteCodeResponse response = roomInviteCodeService.query(room.getId().id());

        assertThat(response.inviteCode()).isEqualTo(room.getInviteCode().code());
    }

    @Test
    void reissue_replacesPrivateRoomInviteCodeForLeader() {
        givenCurrentUser(leaderId);
        PrivateRoom room = PrivateRoom.create("private", leaderId);
        String previousInviteCode = room.getInviteCode().code();
        given(studyRoomReader.getRoom(room.getId().id())).willReturn(room);

        RoomInviteCodeResponse response = roomInviteCodeService.reissue(room.getId().id());

        assertThat(response.inviteCode()).isNotBlank();
        assertThat(response.inviteCode()).isNotEqualTo(previousInviteCode);
        then(roomRepository).should().save(room);
    }

    @Test
    void query_publicRoom_throwsInvalidInviteCodeException() {
        givenCurrentUser(leaderId);
        PublicRoom room = PublicRoom.create("public", leaderId);
        given(studyRoomReader.getRoom(room.getId().id())).willReturn(room);

        assertThatThrownBy(() -> roomInviteCodeService.query(room.getId().id()))
                .isInstanceOf(InvalidInviteCodeException.class);
    }

    @Test
    void query_nonLeader_throwsNotLeaderException() {
        givenCurrentUser(otherUserId);
        PrivateRoom room = PrivateRoom.create("private", leaderId);
        given(studyRoomReader.getRoom(room.getId().id())).willReturn(room);

        assertThatThrownBy(() -> roomInviteCodeService.query(room.getId().id()))
                .isInstanceOf(NotLeaderException.class);
    }
}
