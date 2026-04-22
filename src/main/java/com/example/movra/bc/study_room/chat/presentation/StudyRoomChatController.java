package com.example.movra.bc.study_room.chat.presentation;

import com.example.movra.bc.study_room.chat.application.service.SendChatMessageService;
import com.example.movra.bc.study_room.chat.presentation.dto.ChatErrorPayload;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessagePayload;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessageRequest;
import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;
import com.example.movra.sharedkernel.exception.ValidationErrorMessageResolver;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StudyRoomChatController {

    private final SendChatMessageService sendChatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CurrentUserQuery currentUserQuery;

    @MessageMapping("/rooms/{roomId}/chat")
    public void sendMessage(@DestinationVariable UUID roomId, @Valid @Payload ChatMessageRequest request) {
        AuthenticatedUser user = currentUserQuery.currentUser();

        ChatMessagePayload payload = sendChatMessageService.send(
                roomId,
                user.userId(),
                user.name(),
                request.content()
        );

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/chat", payload);
    }

    @MessageExceptionHandler(CustomException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ChatErrorPayload handleCustomException(CustomException e) {
        return ChatErrorPayload.of(e.getErrorCode());
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ChatErrorPayload handleValidationException(MethodArgumentNotValidException e) {
        String message = ValidationErrorMessageResolver.resolve(e.getBindingResult());
        return ChatErrorPayload.of(ErrorCode.INVALID_REQUEST, message);
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ChatErrorPayload handleUnexpectedException(Exception e) {
        log.error("Unexpected study room chat error", e);
        return ChatErrorPayload.of(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
