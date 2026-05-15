package com.example.movra.bc.study_room.room.application.service.dto.response;

import com.example.movra.bc.study_room.room.domain.vo.InviteCode;

public record RoomInviteCodeResponse(
        String inviteCode
) {
    public static RoomInviteCodeResponse from(InviteCode inviteCode) {
        return new RoomInviteCodeResponse(inviteCode == null ? null : inviteCode.code());
    }
}
