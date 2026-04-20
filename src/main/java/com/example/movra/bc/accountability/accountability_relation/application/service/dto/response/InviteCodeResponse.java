package com.example.movra.bc.accountability.accountability_relation.application.service.dto.response;

import com.example.movra.bc.accountability.accountability_relation.domain.vo.AccountabilityInviteCode;

import java.time.LocalDateTime;

public record InviteCodeResponse(
        String inviteCode,
        LocalDateTime expiresAt
) {

    public static InviteCodeResponse from(AccountabilityInviteCode inviteCode){
        return new InviteCodeResponse(inviteCode.code(), inviteCode.expiresAt());
    }
}
