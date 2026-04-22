package com.example.movra.bc.study_room.chat.presentation;

import com.example.movra.bc.study_room.chat.application.service.SendChatMessageService;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessagePayload;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessageRequest;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class StudyRoomChatController {

    private final SendChatMessageService sendChatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CurrentUserQuery currentUserQuery;

    @MessageMapping("/rooms/{roomId}/chat")
    public void sendMessage(@DestinationVariable UUID roomId, ChatMessageRequest request) {
        AuthenticatedUser user = currentUserQuery.currentUser();

        ChatMessagePayload payload = sendChatMessageService.send(
                roomId,
                user.userId(),
                user.name(),
                request.content()
        );

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/chat", payload);
    }
}
