package com.example.movra.bc.personalization.behavior_profile.presentation;

import com.example.movra.bc.personalization.behavior_profile.application.service.CreateBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.QueryBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.UpdateBehaviorProfileService;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.request.CreateBehaviorProfileRequest;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.request.UpdateBehaviorProfileRequest;
import com.example.movra.bc.personalization.behavior_profile.application.service.dto.response.BehaviorProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/behavior-profiles")
@RequiredArgsConstructor
public class BehaviorProfileController {

    private final CreateBehaviorProfileService createBehaviorProfileService;
    private final QueryBehaviorProfileService queryBehaviorProfileService;
    private final UpdateBehaviorProfileService updateBehaviorProfileService;

    @PostMapping
    public void create(@Valid @RequestBody CreateBehaviorProfileRequest request) {
        createBehaviorProfileService.create(request);
    }

    @GetMapping
    public BehaviorProfileResponse query() {
        return queryBehaviorProfileService.query();
    }

    @PatchMapping
    public void update(@Valid @RequestBody UpdateBehaviorProfileRequest request) {
        updateBehaviorProfileService.update(request);
    }
}
