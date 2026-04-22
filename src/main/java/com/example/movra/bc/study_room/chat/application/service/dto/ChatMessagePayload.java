package com.example.movra.bc.study_room.chat.application.service.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessagePayload(
        UUID roomId,
        UUID senderId,
        String senderName,
        String content,
        Instant sentAt
) {
}
