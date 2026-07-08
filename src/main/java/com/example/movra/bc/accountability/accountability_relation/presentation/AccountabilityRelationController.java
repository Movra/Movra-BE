package com.example.movra.bc.accountability.accountability_relation.presentation;

import com.example.movra.bc.accountability.accountability_relation.application.service.CreateAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.DisconnectAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.JoinAccountabilityRelationService;
import com.example.movra.bc.accountability.accountability_relation.application.service.UpdateVisibilityPolicyService;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.JoinAccountabilityRelationRequest;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.request.VisibilityPolicyRequest;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityRelationResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.FriendAccountabilityStatusResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.InviteCodeResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.dto.response.InviteCodeStatusResponse;
import com.example.movra.bc.accountability.accountability_relation.application.service.invite.QueryInviteCodeStatusService;
import com.example.movra.bc.accountability.accountability_relation.application.service.invite.ReissueInviteCodeService;
import com.example.movra.bc.accountability.accountability_relation.application.service.query.QueryFriendAccountabilityStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accountability-relations")
@RequiredArgsConstructor
public class AccountabilityRelationController {

    private final CreateAccountabilityRelationService createAccountabilityRelationService;
    private final JoinAccountabilityRelationService joinAccountabilityRelationService;
    private final ReissueInviteCodeService reissueInviteCodeService;
    private final QueryInviteCodeStatusService queryInviteCodeStatusService;
    private final QueryFriendAccountabilityStatusService queryFriendAccountabilityStatusService;
    private final UpdateVisibilityPolicyService updateVisibilityPolicyService;
    private final DisconnectAccountabilityRelationService disconnectAccountabilityRelationService;

    @PostMapping
    public void create(@Valid @RequestBody VisibilityPolicyRequest request) {
        createAccountabilityRelationService.create(request);
    }

    @PostMapping("/join")
    public FriendAccountabilityRelationResponse join(@Valid @RequestBody JoinAccountabilityRelationRequest request) {
        return joinAccountabilityRelationService.join(request);
    }

    @PostMapping("/invite-code/reissue")
    public InviteCodeResponse reissueInviteCode() {
        return reissueInviteCodeService.reissue();
    }

    @GetMapping("/invite-code/status")
    public InviteCodeStatusResponse queryInviteCodeStatus() {
        return queryInviteCodeStatusService.query();
    }

    @GetMapping("/friends")
    public FriendAccountabilityStatusResponse queryFriends() {
        return queryFriendAccountabilityStatusService.query();
    }

    @PatchMapping("/visibility-policy")
    public FriendAccountabilityRelationResponse updateVisibilityPolicy(@Valid @RequestBody VisibilityPolicyRequest request) {
        return updateVisibilityPolicyService.update(request);
    }

    @DeleteMapping("/watcher")
    public void removeWatcherFromMyRelation() {
        disconnectAccountabilityRelationService.removeWatcherFromMyRelation();
    }

    @DeleteMapping("/watching")
    public void stopWatchingFriend() {
        disconnectAccountabilityRelationService.stopWatchingFriend();
    }
}
