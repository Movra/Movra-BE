package com.example.movra.bc.study_room.chat.application.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank
        @Size(max = ChatMessageRequest.MAX_CONTENT_LENGTH)
        String content
) {
    public static final int MAX_CONTENT_LENGTH = 500;
}
