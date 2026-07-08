package com.example.movra.bc.account.user.application.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OauthCompleteRequest(
        @NotBlank String code
) {
}
