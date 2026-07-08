package com.example.movra.bc.personalization.behavior_profile.presentation;

import com.example.movra.bc.personalization.behavior_profile.application.service.AcceptProfileAdjustmentService;
import com.example.movra.bc.personalization.behavior_profile.application.service.DismissProfileAdjustmentService;
import com.example.movra.bc.personalization.behavior_profile.application.service.QueryProfileAdjustmentService;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.response.ProfileAdjustmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/behavior-profiles/adjustments")
@RequiredArgsConstructor
public class ProfileAdjustmentController {

    private final QueryProfileAdjustmentService queryProfileAdjustmentService;
    private final AcceptProfileAdjustmentService acceptProfileAdjustmentService;
    private final DismissProfileAdjustmentService dismissProfileAdjustmentService;

    @GetMapping
    public List<ProfileAdjustmentResponse> queryPending() {
        return queryProfileAdjustmentService.queryPending();
    }

    @PostMapping("/{suggestionId}/accept")
    public void accept(@PathVariable UUID suggestionId) {
        acceptProfileAdjustmentService.accept(suggestionId);
    }

    @PostMapping("/{suggestionId}/dismiss")
    public void dismiss(@PathVariable UUID suggestionId) {
        dismissProfileAdjustmentService.dismiss(suggestionId);
    }
}
