package com.example.movra.bc.feedback.tiny_win.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTitleTinyWinRequest(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 30, message = "제목을 30 이내로 입력해주세요.")
        String title
) {
}
