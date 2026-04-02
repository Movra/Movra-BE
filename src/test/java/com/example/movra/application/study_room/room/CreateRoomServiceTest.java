package com.example.movra.application.study_room.room;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.study_room.room.application.service.CreateRoomService;
import com.example.movra.bc.study_room.room.application.service.dto.request.CreateRoomRequest;
import com.example.movra.bc.study_room.room.application.service.dto.response.CreateRoomResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CreateRoomServiceTest {

    @InjectMocks
    private CreateRoomService createRoomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("공개 방 생성 성공")
    void create_publicRoom_success() {
        // given
        givenCurrentUser();

        // when
        CreateRoomResponse response = createRoomService.create(new CreateRoomRequest("스터디룸", Visibility.PUBLIC));

        // then
        assertThat(response.roomId()).isNotNull();
        assertThat(response.inviteCode()).isNull();
        then(roomRepository).should().save(any());
    }

    @Test
    @DisplayName("비공개 방 생성 시 초대 코드 반환")
    void create_privateRoom_returnsInviteCode() {
        // given
        givenCurrentUser();

        // when
        CreateRoomResponse response = createRoomService.create(new CreateRoomRequest("비공개룸", Visibility.PRIVATE));

        // then
        assertThat(response.roomId()).isNotNull();
        assertThat(response.inviteCode()).isNotNull();
        then(roomRepository).should().save(any());
    }
}
