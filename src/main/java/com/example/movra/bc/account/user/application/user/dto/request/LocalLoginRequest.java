package com.example.movra.bc.account.user.application.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocalLoginRequest(
        @NotBlank(message = "account ID는 필수입니다.")
        @Size(max = 30, message = "account ID를 30 사이로 입력해주세요.")
        String accountId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호를 8 ~ 20 사이로 입력해주세요.")
        String password
) {
}
