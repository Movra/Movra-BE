package com.example.movra.application.study_room.room;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.participant.domain.type.SessionMode;
import com.example.movra.bc.study_room.room.application.service.KickParticipantService;
import com.example.movra.bc.study_room.room.domain.Room;
import com.example.movra.bc.study_room.room.domain.exception.LeaderCannotKickSelfException;
import com.example.movra.bc.study_room.room.domain.exception.NotLeaderException;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class KickParticipantServiceTest {

    @InjectMocks
    private KickParticipantService kickParticipantService;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private StudyRoomReader studyRoomReader;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId leaderId = UserId.newId();

    private void givenCurrentUser(UserId userId) {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    @Test
    @DisplayName("리더가 참여자 강퇴 성공")
    void kick_success() {
        // given
        givenCurrentUser(leaderId);
        UserId targetId = UserId.newId();
        Room room = Room.create("스터디룸", leaderId, Visibility.PUBLIC);
        Participant target = Participant.enter(targetId, room.getId());

        given(studyRoomReader.getRoom(any())).willReturn(room);
        given(studyRoomReader.getParticipant(targetId, room.getId())).willReturn(target);

        // when
        kickParticipantService.kick(room.getId().id(), targetId.id());

        // then
        assertThat(target.getSessionMode()).isEqualTo(SessionMode.ENDED);
        then(participantRepository).should().delete(target);
    }

    @Test
    @DisplayName("집중 중인 참여자를 강퇴하면 종료 처리 후 삭제한다")
    void kick_focusingParticipant_recordsAndDeletes() {
        // given
        givenCurrentUser(leaderId);
        UserId targetId = UserId.newId();
        Room room = Room.create("스터디룸", leaderId, Visibility.PUBLIC);
        Participant target = Participant.enter(targetId, room.getId());
        target.startFocus();

        given(studyRoomReader.getRoom(any())).willReturn(room);
        given(studyRoomReader.getParticipant(targetId, room.getId())).willReturn(target);

        // when
        kickParticipantService.kick(room.getId().id(), targetId.id());

        // then
        assertThat(target.isEnded()).isTrue();
        then(participantRepository).should().delete(target);
    }

    @Test
    @DisplayName("리더가 아닌 사용자가 강퇴 시 NotLeaderException 발생")
    void kick_notLeader_throwsException() {
        // given
        UserId nonLeaderId = UserId.newId();
        givenCurrentUser(nonLeaderId);
        UserId targetId = UserId.newId();
        Room room = Room.create("스터디룸", leaderId, Visibility.PUBLIC);

        given(studyRoomReader.getRoom(any())).willReturn(room);

        // when & then
        assertThatThrownBy(() -> kickParticipantService.kick(room.getId().id(), targetId.id()))
                .isInstanceOf(NotLeaderException.class);
    }

    @Test
    @DisplayName("리더가 자기 자신을 강퇴 시 LeaderCannotKickSelfException 발생")
    void kick_self_throwsException() {
        // given
        givenCurrentUser(leaderId);
        Room room = Room.create("스터디룸", leaderId, Visibility.PUBLIC);

        given(studyRoomReader.getRoom(any())).willReturn(room);

        // when & then
        assertThatThrownBy(() -> kickParticipantService.kick(room.getId().id(), leaderId.id()))
                .isInstanceOf(LeaderCannotKickSelfException.class);
    }
}
