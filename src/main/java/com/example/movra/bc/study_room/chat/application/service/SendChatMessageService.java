package com.example.movra.bc.study_room.chat.application.service;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.chat.application.exception.ChatNotAllowedException;
import com.example.movra.bc.study_room.chat.application.exception.InvalidChatMessageException;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessagePayload;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessageRequest;
import com.example.movra.bc.study_room.helper.StudyRoomReader;
import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.type.SessionMode;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendChatMessageService {

    private final StudyRoomReader studyRoomReader;

    public ChatMessagePayload send(UUID roomId, UserId senderId, String senderName, String content) {
        Participant participant = studyRoomReader.getParticipant(senderId, RoomId.of(roomId));

        if (participant.getSessionMode() != SessionMode.REST) {
            throw new ChatNotAllowedException();
        }

        validateContent(content);
        Instant sentAt = Instant.now();

        log.debug("Chat message sent: roomId={}, contentLength={}, timestamp={}",
                roomId, content.length(), sentAt);

        return new ChatMessagePayload(
                roomId,
                senderId.id(),
                senderName,
                content,
                sentAt
        );
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank() || content.length() > ChatMessageRequest.MAX_CONTENT_LENGTH) {
            throw new InvalidChatMessageException();
        }
    }
}
