package com.example.movra.bc.feedback.tiny_win.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateContentTInyWinRequest(

        @NotBlank(message = "본문은 필수입니다.")
        @Size(max = 3000, message = "내용을 3000자 이내로 작성해주세요")
        String content
) {
}
