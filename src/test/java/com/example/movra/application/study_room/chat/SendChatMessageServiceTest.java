package com.example.movra.application.study_room.chat;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.chat.application.exception.ChatNotAllowedException;
import com.example.movra.bc.study_room.chat.application.exception.InvalidChatMessageException;
import com.example.movra.bc.study_room.chat.application.service.SendChatMessageService;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessagePayload;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessageRequest;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SendChatMessageServiceTest {

    @InjectMocks
    private SendChatMessageService sendChatMessageService;

    @Mock
    private StudyRoomReader studyRoomReader;

    private final UserId userId = UserId.newId();
    private final UUID roomId = UUID.randomUUID();

    @Test
    @DisplayName("send succeeds when participant is resting and content is valid")
    void send_restingParticipant_success() {
        Participant participant = restingParticipant();
        given(studyRoomReader.getParticipant(userId, RoomId.of(roomId))).willReturn(participant);

        ChatMessagePayload payload = sendChatMessageService.send(roomId, userId, "tester", "휴식 잘 하고 있나요?");

        assertThat(payload.roomId()).isEqualTo(roomId);
        assertThat(payload.senderId()).isEqualTo(userId.id());
        assertThat(payload.senderName()).isEqualTo("tester");
        assertThat(payload.content()).isEqualTo("휴식 잘 하고 있나요?");
        assertThat(payload.sentAt()).isNotNull();
    }

    @Test
    @DisplayName("send throws when participant is not resting")
    void send_notResting_throwsException() {
        Participant participant = Participant.enter(userId, RoomId.of(roomId));
        given(studyRoomReader.getParticipant(userId, RoomId.of(roomId))).willReturn(participant);

        assertThatThrownBy(() -> sendChatMessageService.send(roomId, userId, "tester", "hello"))
                .isInstanceOf(ChatNotAllowedException.class);
    }

    @Test
    @DisplayName("send throws when content is null")
    void send_nullContent_throwsException() {
        Participant participant = restingParticipant();
        given(studyRoomReader.getParticipant(userId, RoomId.of(roomId))).willReturn(participant);

        assertThatThrownBy(() -> sendChatMessageService.send(roomId, userId, "tester", null))
                .isInstanceOf(InvalidChatMessageException.class);
    }

    @Test
    @DisplayName("send throws when content is blank")
    void send_blankContent_throwsException() {
        Participant participant = restingParticipant();
        given(studyRoomReader.getParticipant(userId, RoomId.of(roomId))).willReturn(participant);

        assertThatThrownBy(() -> sendChatMessageService.send(roomId, userId, "tester", "   "))
                .isInstanceOf(InvalidChatMessageException.class);
    }

    @Test
    @DisplayName("send throws when content is too long")
    void send_tooLongContent_throwsException() {
        Participant participant = restingParticipant();
        given(studyRoomReader.getParticipant(userId, RoomId.of(roomId))).willReturn(participant);
        String tooLongContent = "a".repeat(ChatMessageRequest.MAX_CONTENT_LENGTH + 1);

        assertThatThrownBy(() -> sendChatMessageService.send(roomId, userId, "tester", tooLongContent))
                .isInstanceOf(InvalidChatMessageException.class);
    }

    private Participant restingParticipant() {
        Participant participant = Participant.enter(userId, RoomId.of(roomId));
        participant.takeBreak();
        return participant;
    }
}
