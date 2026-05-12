package com.example.movra.bc.focus.focus_session.application.service.dto.request;

import jakarta.validation.constraints.NotNull;

public record StartFocusSessionRequest(
        @NotNull Integer presetMinutes
) {
}
