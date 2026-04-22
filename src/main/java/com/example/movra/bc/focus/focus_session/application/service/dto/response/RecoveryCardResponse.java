package com.example.movra.bc.focus.focus_session.application.service.dto.response;

import com.example.movra.bc.focus.focus_session.domain.type.RecoveryType;
import lombok.Builder;

@Builder
public record RecoveryCardResponse(
        boolean needsRecovery,
        RecoveryType recoveryType,
        String suggestedAction,
        long yesterdayFocusSeconds,
        double yesterdayTopPickCompletionRate
) {
}
