package com.example.movra.bc.accountability.accountability_relation.domain.vo;

import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Embeddable
public class VisibilityPolicy {

    @ElementCollection(targetClass = MonitoringTarget.class)
    @CollectionTable(
            name = "accountability_relation_allowed_targets",
            joinColumns = @JoinColumn(name = "accountability_relation_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "monitoring_target")
    private Set<MonitoringTarget> allowedTargets = new HashSet<>();

    protected VisibilityPolicy() {
    }

    public VisibilityPolicy(Set<MonitoringTarget> allowedTargets) {
        this.allowedTargets = allowedTargets;
    }
}
