package com.example.movra.bc.accountability.accountability_relation.domain.vo;

import jakarta.persistence.Embeddable;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
public record AccountabilityInviteCode(
        String code,
        LocalDateTime expiresAt
) {

    public static AccountabilityInviteCode generate(Clock clock) {
        String code = UUID.randomUUID().toString().substring(0, 10);
        return new AccountabilityInviteCode(code, LocalDateTime.now(clock).plusMinutes(5));
    }

    public boolean isExpired(Clock clock) {
        return LocalDateTime.now(clock).isAfter(expiresAt);
    }
}
