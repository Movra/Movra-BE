package com.example.movra.bc.accountability.accountability_relation.application.service.dto.response;

import com.example.movra.bc.accountability.accountability_relation.domain.AccountabilityRelation;
import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record FriendAccountabilityRelationResponse(
        UUID accountabilityRelationId,
        UUID subjectUserId,
        UUID watcherUserId,
        boolean watcherConnected,
        Set<MonitoringTarget> allowedTargets
) {

    public static FriendAccountabilityRelationResponse from(AccountabilityRelation relation) {
        return FriendAccountabilityRelationResponse.builder()
                .accountabilityRelationId(relation.getId().id())
                .subjectUserId(relation.getSubjectUserId().id())
                .watcherUserId(relation.getWatcherUserId() == null ? null : relation.getWatcherUserId().id())
                .watcherConnected(relation.getWatcherUserId() != null)
                .allowedTargets(relation.getVisibilityPolicy().getAllowedTargets())
                .build();
    }
}
