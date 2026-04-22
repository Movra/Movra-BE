package com.example.movra.presentation.study_room.chat;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.study_room.chat.application.exception.ChatNotAllowedException;
import com.example.movra.bc.study_room.chat.application.service.SendChatMessageService;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessagePayload;
import com.example.movra.bc.study_room.chat.application.service.dto.ChatMessageRequest;
import com.example.movra.bc.study_room.chat.presentation.StudyRoomChatController;
import com.example.movra.bc.study_room.chat.presentation.dto.ChatErrorPayload;
import com.example.movra.sharedkernel.exception.ErrorCode;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class StudyRoomChatControllerTest {

    @Mock
    private SendChatMessageService sendChatMessageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder()
                        .userId(userId)
                        .name("tester")
                        .build()
        );
    }

    @Test
    @DisplayName("sendMessage broadcasts payload to room topic")
    void sendMessage_success() {
        givenCurrentUser();
        StudyRoomChatController controller = new StudyRoomChatController(
                sendChatMessageService,
                messagingTemplate,
                currentUserQuery
        );
        UUID roomId = UUID.randomUUID();
        ChatMessagePayload payload = new ChatMessagePayload(
                roomId,
                userId.id(),
                "tester",
                "hello",
                Instant.parse("2026-04-22T12:00:00Z")
        );
        given(sendChatMessageService.send(roomId, userId, "tester", "hello")).willReturn(payload);

        controller.sendMessage(roomId, new ChatMessageRequest("hello"));

        then(messagingTemplate).should().convertAndSend("/topic/rooms/" + roomId + "/chat", payload);
    }

    @Test
    @DisplayName("handleCustomException returns error payload")
    void handleCustomException_returnsErrorPayload() {
        StudyRoomChatController controller = new StudyRoomChatController(
                sendChatMessageService,
                messagingTemplate,
                currentUserQuery
        );

        ChatErrorPayload payload = controller.handleCustomException(new ChatNotAllowedException());

        assertThat(payload.statusCode()).isEqualTo(ErrorCode.CHAT_NOT_ALLOWED.getHttpStatus().value());
        assertThat(payload.message()).isEqualTo(ErrorCode.CHAT_NOT_ALLOWED.getMessage());
        assertThat(payload.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleUnexpectedException returns internal server error payload")
    void handleUnexpectedException_returnsInternalServerErrorPayload() {
        StudyRoomChatController controller = new StudyRoomChatController(
                sendChatMessageService,
                messagingTemplate,
                currentUserQuery
        );

        ChatErrorPayload payload = controller.handleUnexpectedException(new IllegalStateException("boom"));

        assertThat(payload.statusCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value());
        assertThat(payload.message()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        assertThat(payload.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleValidationException returns first field validation message")
    void handleValidationException_returnsFieldValidationMessage() throws NoSuchMethodException {
        StudyRoomChatController controller = new StudyRoomChatController(
                sendChatMessageService,
                messagingTemplate,
                currentUserQuery
        );
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new ChatMessageRequest(""), "chatMessageRequest");
        bindingResult.rejectValue("content", "NotBlank", "must not be blank");
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new MethodParameter(
                        StudyRoomChatController.class.getMethod("sendMessage", UUID.class, ChatMessageRequest.class),
                        1
                ),
                bindingResult
        );

        ChatErrorPayload payload = controller.handleValidationException(exception);

        assertThat(payload.statusCode()).isEqualTo(ErrorCode.INVALID_REQUEST.getHttpStatus().value());
        assertThat(payload.message()).isEqualTo("content: must not be blank");
        assertThat(payload.timestamp()).isNotNull();
    }
}
