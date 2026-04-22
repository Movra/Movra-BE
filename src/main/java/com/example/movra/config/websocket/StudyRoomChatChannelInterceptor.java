package com.example.movra.config.websocket;

import com.example.movra.bc.account.user.application.user.internal.TokenService;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.account.user.infrastructure.user.security.auth.AuthDetails;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtProperties;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.exception.InvalidJwtException;
import com.example.movra.bc.study_room.chat.application.exception.InvalidChatDestinationException;
import com.example.movra.bc.study_room.participant.application.exception.ParticipantNotFoundException;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.domain.vo.RoomId;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class StudyRoomChatChannelInterceptor implements ChannelInterceptor {

    private static final Pattern CHAT_SEND_DESTINATION_PATTERN =
            Pattern.compile("^/app/rooms/([0-9a-fA-F\\-]{36})/chat$");
    private static final Pattern CHAT_SUBSCRIBE_DESTINATION_PATTERN =
            Pattern.compile("^/topic/rooms/([0-9a-fA-F\\-]{36})/chat$");

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final TokenService tokenService;
    private final ParticipantRepository participantRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            authenticate(accessor);
            return message;
        }

        if (accessor.getCommand() == StompCommand.SEND) {
            authorizeSend(accessor);
        }

        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeSubscribe(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String headerValue = accessor.getFirstNativeHeader(jwtProperties.header());
        String token = jwtTokenProvider.resolveToken(headerValue);
        if (token == null) {
            throw new InvalidJwtException();
        }

        UserDetails details = new AuthDetails(tokenService.authenticate(token));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                details,
                null,
                details.getAuthorities()
        );
        accessor.setUser(authentication);
    }

    private void authorizeSend(StompHeaderAccessor accessor) {
        if (extractChatRoomId(accessor.getDestination(), CHAT_SUBSCRIBE_DESTINATION_PATTERN) != null) {
            throw new InvalidChatDestinationException();
        }

        UUID roomId = extractChatRoomId(accessor.getDestination(), CHAT_SEND_DESTINATION_PATTERN);
        if (roomId == null) {
            return;
        }

        authorizeParticipant(accessor.getUser(), roomId);
    }

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        if (extractChatRoomId(accessor.getDestination(), CHAT_SEND_DESTINATION_PATTERN) != null) {
            throw new InvalidChatDestinationException();
        }

        UUID roomId = extractChatRoomId(accessor.getDestination(), CHAT_SUBSCRIBE_DESTINATION_PATTERN);
        if (roomId == null) {
            return;
        }

        authorizeParticipant(accessor.getUser(), roomId);
    }

    private void authorizeParticipant(Principal principal, UUID roomId) {
        UserId userId = extractUserId(principal);
        if (!participantRepository.existsByUserIdAndRoomId(userId, RoomId.of(roomId))) {
            throw new ParticipantNotFoundException();
        }
    }

    private UUID extractChatRoomId(String destination, Pattern pattern) {
        if (destination == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(destination);
        if (!matcher.matches()) {
            return null;
        }

        return UUID.fromString(matcher.group(1));
    }

    private UserId extractUserId(Principal principal) {
        if (!(principal instanceof Authentication authentication)
                || !(authentication.getPrincipal() instanceof AuthDetails authDetails)) {
            throw new InvalidJwtException();
        }

        return authDetails.getUser().getId();
    }
}
