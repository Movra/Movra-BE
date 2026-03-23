package com.example.movra.bc.feedback.tiny_win.application.service.dto.response;

import com.example.movra.bc.feedback.tiny_win.domain.TinyWin;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record TinyWinResponse(
        UUID tinyWinId,
        String title,
        String content,
        LocalDate localDate
) {

    public static TinyWinResponse from(TinyWin tinyWin) {
        return TinyWinResponse.builder()
                .tinyWinId(tinyWin.getId().id())
                .title(tinyWin.getTitle())
                .content(tinyWin.getContent())
                .localDate(tinyWin.getLocalDate())
                .build();
    }
}
