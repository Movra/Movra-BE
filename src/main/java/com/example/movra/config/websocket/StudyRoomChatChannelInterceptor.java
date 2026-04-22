package com.example.movra.config.websocket;

import com.example.movra.bc.account.user.application.user.internal.TokenService;
import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.account.user.infrastructure.user.security.auth.AuthDetails;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtProperties;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenProvider;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.exception.InvalidJwtException;
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

    private static final Pattern CHAT_DESTINATION_PATTERN =
            Pattern.compile("^/(app|topic)/rooms/([0-9a-fA-F\\-]{36})/chat$");

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

        if (accessor.getCommand() == StompCommand.SEND || accessor.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeChatAccess(accessor);
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

    private void authorizeChatAccess(StompHeaderAccessor accessor) {
        UUID roomId = extractChatRoomId(accessor.getDestination());
        if (roomId == null) {
            return;
        }

        UserId userId = extractUserId(accessor.getUser());
        if (!participantRepository.existsByUserIdAndRoomId(userId, RoomId.of(roomId))) {
            throw new ParticipantNotFoundException();
        }
    }

    private UUID extractChatRoomId(String destination) {
        if (destination == null) {
            return null;
        }

        Matcher matcher = CHAT_DESTINATION_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return null;
        }

        return UUID.fromString(matcher.group(2));
    }

    private UserId extractUserId(Principal principal) {
        if (!(principal instanceof Authentication authentication)
                || !(authentication.getPrincipal() instanceof AuthDetails authDetails)) {
            throw new InvalidJwtException();
        }

        return authDetails.getUser().getId();
    }
}
