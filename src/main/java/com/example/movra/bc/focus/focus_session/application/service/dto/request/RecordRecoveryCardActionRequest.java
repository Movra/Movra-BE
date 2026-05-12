package com.example.movra.bc.focus.focus_session.application.service.dto.request;

import com.example.movra.bc.focus.focus_session.domain.type.RecoveryCardAction;
import jakarta.validation.constraints.NotNull;

public record RecordRecoveryCardActionRequest(
        @NotNull RecoveryCardAction action
) {
}
