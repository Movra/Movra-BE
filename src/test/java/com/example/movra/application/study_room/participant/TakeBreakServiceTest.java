package com.example.movra.application.study_room.participant;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.application.service.TakeBreakService;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.exception.NotFocusingException;
import com.example.movra.bc.study_room.participant.domain.type.SessionMode;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TakeBreakServiceTest {

    @InjectMocks
    private TakeBreakService takeBreakService;

    @Mock
    private StudyRoomReader studyRoomReader;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();
    private final RoomId roomId = RoomId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("휴식 전환 성공")
    void takeBreak_success() {
        // given
        givenCurrentUser();
        Participant participant = Participant.enter(userId, roomId);
        participant.startFocus();
        given(studyRoomReader.getParticipant(userId, roomId)).willReturn(participant);

        // when
        takeBreakService.takeBreak(roomId.id());

        // then
        assertThat(participant.getSessionMode()).isEqualTo(SessionMode.REST);
    }

    @Test
    @DisplayName("집중 중이 아닐 때 휴식 전환 시 NotFocusingException 발생")
    void takeBreak_notFocusing_throwsException() {
        // given
        givenCurrentUser();
        Participant participant = Participant.enter(userId, roomId);
        given(studyRoomReader.getParticipant(userId, roomId)).willReturn(participant);

        // when & then
        assertThatThrownBy(() -> takeBreakService.takeBreak(roomId.id()))
                .isInstanceOf(NotFocusingException.class);
    }
}
