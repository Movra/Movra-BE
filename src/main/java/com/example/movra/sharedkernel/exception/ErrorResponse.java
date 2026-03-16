package com.example.movra.sharedkernel.exception;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        HttpStatus httpStatus,
        int statusCode,
        String message,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(ErrorCode errorCode){
        return ErrorResponse.builder()
                .httpStatus(errorCode.getHttpStatus())
                .statusCode(errorCode.getHttpStatus().value())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .httpStatus(errorCode.getHttpStatus())
                .statusCode(errorCode.getHttpStatus().value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
