package com.example.movra.bc.accountability.accountability_relation.application.service.dto.response;

import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.vo.AccountabilityInviteCode;

import java.time.Clock;
import java.time.LocalDateTime;

public record InviteCodeStatusResponse(
        String inviteCode,
        LocalDateTime expiredAt,
        boolean expired, // 초대 코드가 만료되었거나, 없는 것에 대한 여부
        boolean reissuable, //초대 코드를 재생성 가능한지에 대한 여부 -> 이미 watcher 가 있다면 생성 X
        boolean watcherConnected //
) {
    public static InviteCodeStatusResponse from(AccountabilityRelation relation, Clock clock) {
        AccountabilityInviteCode inviteCode = relation.getInviteCode();
        boolean watcherConnected = relation.getWatcherUserId() != null;

        return new InviteCodeStatusResponse(
                inviteCode == null ? null : inviteCode.code(),
                inviteCode == null ? null : inviteCode.expiresAt(),
                inviteCode == null || inviteCode.isExpired(clock),
                !watcherConnected,
                watcherConnected
        );

    }
}
