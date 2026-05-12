package com.example.movra.bc.accountability.accountability_relation.application.service.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FriendAccountabilityStatusResponse(
        List<FriendAccountabilityRelationResponse> watchedByFriends,
        List<FriendAccountabilityRelationResponse> watchingFriends
) {
}
