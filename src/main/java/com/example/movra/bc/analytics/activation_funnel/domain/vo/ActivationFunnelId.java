package com.example.movra.bc.analytics.activation_funnel.domain.vo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record ActivationFunnelId(UUID id) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ActivationFunnelId newId() {
        return new ActivationFunnelId(UUID.randomUUID());
    }

    public static ActivationFunnelId of(UUID id) {
        return new ActivationFunnelId(id);
    }
}
