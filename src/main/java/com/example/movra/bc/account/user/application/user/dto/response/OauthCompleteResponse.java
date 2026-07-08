package com.example.movra.bc.account.user.application.user.dto.response;

import lombok.Builder;

@Builder
public record OauthCompleteResponse(
        String accessToken,
        String refreshToken,
        String pendingToken,
        boolean isProfileCompleted
) {
}
