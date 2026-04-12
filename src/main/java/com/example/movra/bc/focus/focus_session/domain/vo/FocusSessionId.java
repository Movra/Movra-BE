package com.example.movra.bc.focus.focus_session.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record FocusSessionId(
        UUID id
) implements Serializable {

    public static FocusSessionId newId() {
        return new FocusSessionId(UUID.randomUUID());
    }

    public static FocusSessionId of(UUID id) {
        return new FocusSessionId(id);
    }
}
