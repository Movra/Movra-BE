package com.example.movra.application.study_room.participant.event;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.participant.application.event.RoomCreatedEventHandler;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.domain.event.RoomCreatedEvent;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RoomCreatedEventHandlerTest {

    @InjectMocks
    private RoomCreatedEventHandler roomCreatedEventHandler;

    @Mock
    private ParticipantRepository participantRepository;

    @Test
    @DisplayName("방 생성 이벤트 발생 시 방장을 참여자로 등록")
    void handle_success() {
        // given
        RoomId roomId = RoomId.newId();
        UserId userId = UserId.newId();
        RoomCreatedEvent event = new RoomCreatedEvent(roomId, userId);

        // when
        roomCreatedEventHandler.handle(event);

        // then
        ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);
        then(participantRepository).should().save(captor.capture());

        Participant participant = captor.getValue();
        assertThat(participant.getRoomId()).isEqualTo(roomId);
        assertThat(participant.getUserId()).isEqualTo(userId);
    }
}
