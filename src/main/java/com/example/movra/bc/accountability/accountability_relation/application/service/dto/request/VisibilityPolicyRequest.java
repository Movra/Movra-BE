package com.example.movra.bc.accountability.accountability_relation.application.service.dto.request;

import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record VisibilityPolicyRequest(

        @NotEmpty
        Set<MonitoringTarget> targets
) {
}
