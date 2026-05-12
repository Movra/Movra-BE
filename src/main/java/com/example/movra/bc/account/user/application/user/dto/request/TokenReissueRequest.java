package com.example.movra.bc.account.user.application.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(
        @NotBlank
        String refreshToken
) {
}
