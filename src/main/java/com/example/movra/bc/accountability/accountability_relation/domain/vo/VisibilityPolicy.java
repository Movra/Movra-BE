package com.example.movra.bc.accountability.accountability_relation.domain.vo;

import com.example.movra.bc.accountability.accountability_relation.domain.type.MonitoringTarget;
import com.example.movra.bc.accountability.accountability_relation.domain.exception.MonitoringTargetNotAllowedException;
import jakarta.persistence.*;

import java.util.Collections;
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
        this.allowedTargets = new HashSet<>(allowedTargets);
    }

    public Set<MonitoringTarget> getAllowedTargets() {
        return Collections.unmodifiableSet(allowedTargets);
    }

    public boolean allows(MonitoringTarget target) {
        return allowedTargets.contains(target);
    }

    public void validateAllowed(MonitoringTarget target) {
        if (!allows(target)) {
            throw new MonitoringTargetNotAllowedException();
        }
    }
}
