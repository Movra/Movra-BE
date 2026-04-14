package com.example.movra.bc.accountability.accountability_relation.domain.vo;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
public record AccountabilityInviteCode(
        String code,
        LocalDateTime expiresAt
) {

    public static AccountabilityInviteCode generate(){
        String code = UUID.randomUUID().toString().substring(0, 10);
        return new AccountabilityInviteCode(code, LocalDateTime.now().plusMinutes(5));
    }

    public static AccountabilityInviteCode of(String code, LocalDateTime expiresAt){
        return new AccountabilityInviteCode(code, expiresAt);
    }

    public boolean isExpired(){
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
