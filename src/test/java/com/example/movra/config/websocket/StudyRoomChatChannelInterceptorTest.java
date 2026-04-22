package com.example.movra.config.websocket;

import com.example.movra.bc.account.user.application.user.internal.TokenService;
import com.example.movra.bc.account.user.domain.user.User;
import com.example.movra.bc.account.user.infrastructure.user.security.auth.AuthDetails;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtProperties;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.exception.InvalidJwtException;
import com.example.movra.bc.study_room.participant.application.exception.ParticipantNotFoundException;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudyRoomChatChannelInterceptorTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenService tokenService;

    @Mock
    private ParticipantRepository participantRepository;

    private final JwtProperties jwtProperties =
            new JwtProperties("test-secret", 3600L, 7200L, "Authorization", "Bearer ");

    @Test
    @DisplayName("CONNECT authenticates user from Authorization header")
    void preSend_connectWithToken_setsUser() {
        StudyRoomChatChannelInterceptor interceptor = new StudyRoomChatChannelInterceptor(
                jwtTokenProvider,
                jwtProperties,
                tokenService,
                participantRepository
        );
        User user = User.createLocalUser("tester", "tester", "image", "tester@example.com", "pw");
        Message<byte[]> message = connectMessage("Bearer valid-token");
        given(jwtTokenProvider.resolveToken("Bearer valid-token")).willReturn("valid-token");
        given(tokenService.authenticate("valid-token")).willReturn(user);

        Message<?> intercepted = interceptor.preSend(message, null);
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(intercepted);

        assertThat(accessor.getUser()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        Authentication authentication = (Authentication) accessor.getUser();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthDetails.class);
        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        assertThat(authDetails.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("CONNECT throws when Authorization header is missing")
    void preSend_connectWithoutToken_throwsException() {
        StudyRoomChatChannelInterceptor interceptor = new StudyRoomChatChannelInterceptor(
                jwtTokenProvider,
                jwtProperties,
                tokenService,
                participantRepository
        );
        Message<byte[]> message = connectMessage(null);
        given(jwtTokenProvider.resolveToken((String) null)).willReturn(null);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    @DisplayName("SEND to chat destination throws when user is unauthenticated")
    void preSend_sendWithoutAuthentication_throwsException() {
        StudyRoomChatChannelInterceptor interceptor = new StudyRoomChatChannelInterceptor(
                jwtTokenProvider,
                jwtProperties,
                tokenService,
                participantRepository
        );
        Message<byte[]> message = destinationMessage(StompCommand.SEND, "/app/rooms/" + UUID.randomUUID() + "/chat", null);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    @DisplayName("SUBSCRIBE to chat destination throws when user is not a participant")
    void preSend_subscribeWithoutMembership_throwsException() {
        StudyRoomChatChannelInterceptor interceptor = new StudyRoomChatChannelInterceptor(
                jwtTokenProvider,
                jwtProperties,
                tokenService,
                participantRepository
        );
        User user = User.createLocalUser("tester", "tester", "image", "tester@example.com", "pw");
        UUID roomId = UUID.randomUUID();
        Principal principal = authenticatedPrincipal(user);
        Message<byte[]> message = destinationMessage(StompCommand.SUBSCRIBE, "/topic/rooms/" + roomId + "/chat", principal);
        given(participantRepository.existsByUserIdAndRoomId(user.getId(), RoomId.of(roomId))).willReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(ParticipantNotFoundException.class);
    }

    @Test
    @DisplayName("SEND to chat destination passes when authenticated participant belongs to room")
    void preSend_sendWithMembership_success() {
        StudyRoomChatChannelInterceptor interceptor = new StudyRoomChatChannelInterceptor(
                jwtTokenProvider,
                jwtProperties,
                tokenService,
                participantRepository
        );
        User user = User.createLocalUser("tester", "tester", "image", "tester@example.com", "pw");
        UUID roomId = UUID.randomUUID();
        Principal principal = authenticatedPrincipal(user);
        Message<byte[]> message = destinationMessage(StompCommand.SEND, "/app/rooms/" + roomId + "/chat", principal);
        given(participantRepository.existsByUserIdAndRoomId(user.getId(), RoomId.of(roomId))).willReturn(true);

        Message<?> intercepted = interceptor.preSend(message, null);

        assertThat(intercepted).isSameAs(message);
    }

    private Message<byte[]> connectMessage(String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorizationHeader != null) {
            accessor.setNativeHeader(jwtProperties.header(), authorizationHeader);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<byte[]> destinationMessage(StompCommand command, String destination, Principal principal) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        accessor.setUser(principal);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Principal authenticatedPrincipal(User user) {
        AuthDetails authDetails = new AuthDetails(user);
        return new UsernamePasswordAuthenticationToken(authDetails, null, authDetails.getAuthorities());
    }
}
