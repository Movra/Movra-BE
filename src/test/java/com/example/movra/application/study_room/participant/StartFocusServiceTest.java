package com.example.movra.application.study_room.participant;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.application.service.StartFocusService;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.exception.AlreadyFocusingException;
import com.example.movra.bc.study_room.participant.domain.exception.ParticipantAlreadyEndedException;
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
class StartFocusServiceTest {

    @InjectMocks
    private StartFocusService startFocusService;

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
    @DisplayName("집중 시작 성공")
    void start_success() {
        // given
        givenCurrentUser();
        Participant participant = Participant.enter(userId, roomId);
        given(studyRoomReader.getParticipant(userId, roomId)).willReturn(participant);

        // when
        startFocusService.start(roomId.id());

        // then
        assertThat(participant.getSessionMode()).isEqualTo(SessionMode.FOCUS);
    }

    @Test
    @DisplayName("휴식 중일 때 집중 시작 성공")
    void start_fromRest_success() {
        // given
        givenCurrentUser();
        Participant participant = Participant.enter(userId, roomId);
        participant.startFocus();
        participant.takeBreak();
        given(studyRoomReader.getParticipant(userId, roomId)).willReturn(participant);

        // when
        startFocusService.start(roomId.id());

        // then
        assertThat(participant.getSessionMode()).isEqualTo(SessionMode.FOCUS);
    }

    @Test
    @DisplayName("이미 집중 중일 때 집중 시작 시 AlreadyFocusingException 발생")
    void start_alreadyFocusing_throwsException() {
        // given
        givenCurrentUser();
        Participant participant = Participant.enter(userId, roomId);
        participant.startFocus();
        given(studyRoomReader.getParticipant(userId, roomId)).willReturn(participant);

        // when & then
        assertThatThrownBy(() -> startFocusService.start(roomId.id()))
                .isInstanceOf(AlreadyFocusingException.class);
    }

    @Test
    @DisplayName("퇴장 완료 상태에서 집중 시작 시 ParticipantAlreadyEndedException 발생")
    void start_ended_throwsException() {
        // given
        givenCurrentUser();
        Participant participant = Participant.enter(userId, roomId);
        participant.leaveAndRecordTime();
        given(studyRoomReader.getParticipant(userId, roomId)).willReturn(participant);

        // when & then
        assertThatThrownBy(() -> startFocusService.start(roomId.id()))
                .isInstanceOf(ParticipantAlreadyEndedException.class);
    }
}
