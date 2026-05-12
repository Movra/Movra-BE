package com.example.movra.bc.account.user.application.user.dto.response;

import lombok.Builder;

@Builder
public record OnboardingContextResponse(
        boolean pendingSchoolHours
) {
}
