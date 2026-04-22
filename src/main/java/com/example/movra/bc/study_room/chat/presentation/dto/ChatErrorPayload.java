package com.example.movra.bc.study_room.chat.presentation.dto;

import com.example.movra.sharedkernel.exception.ErrorCode;

import java.time.Instant;

public record ChatErrorPayload(
        int statusCode,
        String message,
        Instant timestamp
) {

    public static ChatErrorPayload of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage());
    }

    public static ChatErrorPayload of(ErrorCode errorCode, String message) {
        return new ChatErrorPayload(
                errorCode.getHttpStatus().value(),
                message,
                Instant.now()
        );
    }
}
