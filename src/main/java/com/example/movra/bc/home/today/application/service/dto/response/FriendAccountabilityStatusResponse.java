package com.example.movra.bc.home.today.application.service.dto.response;

import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.InviteCodeStatusResponse;
import lombok.Builder;

@Builder
public record FriendAccountabilityStatusResponse(
        boolean relationCreated,
        boolean watchedByFriend,
        boolean watchingFriend,
        InviteCodeStatusResponse inviteCodeStatus
) {
}
